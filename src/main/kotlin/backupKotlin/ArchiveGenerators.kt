package backupKotlin

import com.google.common.annotations.VisibleForTesting
import com.google.common.io.ByteStreams
import mu.KotlinLogging
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

private val KNOWN_PACKAGE_MANAGERS: Map<PackageManagerName, ListGenerator> =
        mapOf(Pair("brew", "brew leaves"))

private val log = KotlinLogging.logger {  }

@VisibleForTesting
internal fun sweepInput(packageManager: PackageManager) {
    require(packageManager.name.isNotBlank())
    if (packageManager.listGenerator.isNullOrBlank()) {
        packageManager.listGenerator = KNOWN_PACKAGE_MANAGERS[packageManager.name]
    }
    requireNotNull(packageManager.listGenerator)
}

private fun getOutputStreamForFile(file: Path) : OutputStream = FileOutputStream(file.toFile(), false)

fun getArchivePath(packageManager: PackageManager): Path =
        Paths.get(".", "archives", "${packageManager.name}-package.txt")

class ArchiveGenerator {
    fun generate(packageManager: PackageManager) {
        sweepInput(packageManager)

        val process = Runtime.getRuntime().exec(packageManager.listGenerator)
        ByteStreams.copy(process.inputStream, getOutputStreamForFile(getArchivePath(packageManager)))
        process.waitFor(100L, TimeUnit.MILLISECONDS)
    }
}
