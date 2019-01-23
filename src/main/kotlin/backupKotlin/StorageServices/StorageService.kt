package backupKotlin.StorageServices

import backupKotlin.Updater
import com.google.common.io.ByteStreams.toByteArray
import mu.KotlinLogging
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.concurrent.TimeUnit

interface StorageService {
    fun upload(updater: Updater, archivePath: Path)
}

val log = KotlinLogging.logger {  }
class CustomStorageService : StorageService {

    private fun sweepInput(updater: Updater) {
        require(updater.name.isNotBlank() && !updater.uploadCommand.isNullOrBlank())
    }

    override fun upload(updater: Updater, archivePath: Path) {
        sweepInput(updater)

        val command = "cat $archivePath | ${updater.uploadCommand}"
        log.info { "Executing: $command" }
        val process = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", command))
        process.waitFor(100, TimeUnit.MILLISECONDS)
        log.warn { toByteArray(process.errorStream).toString(Charset.defaultCharset()) }
    }
}
