package backupKotlin

import arrow.core.Try
import arrow.effects.IO
import backupKotlin.StorageServices.FileReader
import com.google.common.io.ByteStreams
import mu.KotlinLogging
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths

private val KNOWN_PACKAGE_MANAGERS: Map<PackageManagerName, ListGenerator> =
        mapOf("cargo" to "/Users/kvwu/.cargo/bin/cargo install --list",
                "yarn" to "cat /Users/kvwu/.config/yarn/global/package.json",
                "conda" to "conda list")

private val log = KotlinLogging.logger {  }

internal fun sweepInput(packageManager: PackageManager) {
    Try {
        require(packageManager.name.isNotBlank())
        if (packageManager.listGenerator.isNullOrBlank()) {
            packageManager.listGenerator = KNOWN_PACKAGE_MANAGERS[packageManager.name]
        }
    requireNotNull(packageManager.listGenerator)
    }.fold({log.error(it) {  }; throw it}, {Unit})
}

private fun getOutputStreamForFile(file: Path) : OutputStream = FileOutputStream(file.toFile(), false)

fun getArchivePath(packageManager: PackageManager): Path =
        Paths.get(".", "archives", "${packageManager.name}-backup.txt")

class ArchiveGenerator(private val fileReader: FileReader) {
    fun generate(packageManager: PackageManager): IO<Unit> {
        sweepInput(packageManager)
        return IO {
            val process = Runtime.getRuntime().exec(packageManager.listGenerator)
            if (getArchivePath(packageManager).toFile().exists()) {
                updateIfDifferent(process, packageManager)
            } else {
                val outputStream = getOutputStreamForFile(getArchivePath(packageManager))
                ByteStreams.copy(process.inputStream, outputStream)
                outputStream.close()
            }
            closeProcessStreams(process)
        }
    }

    private fun updateIfDifferent(process: Process, packageManager: PackageManager) {
        val list = process.inputStream.readBytes()
        val oldList = fileReader.readAsByteArray(getArchivePath(packageManager))
        if (!list.contentEquals(oldList)) {
            fileReader.writeFiles(getArchivePath(packageManager), list)
        } else {
            log.info { "No updates to ${packageManager.name}" }
        }
    }
}
