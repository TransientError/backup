package backupKotlin.StorageServices

import arrow.core.Try
import backupKotlin.Updater
import backupKotlin.closeProcessAndStreams
import backupKotlin.logIfNotEmpty
import mu.KotlinLogging
import java.nio.file.Path
import java.util.concurrent.TimeUnit

interface StorageService {
    fun upload(updater: Updater, archivePath: Path)
}

val log = KotlinLogging.logger {  }
class CustomStorageService : StorageService {

    private fun sweepInput(updater: Updater) {
        Try {
            require(updater.name.isNotBlank() && !updater.uploadCommand.isNullOrBlank())
        }.fold({log.error(it) {  }; throw it}, {Unit})
    }

    override fun upload(updater: Updater, archivePath: Path) {
        sweepInput(updater)
        log.info { "Executing: ${updater.uploadCommand}" }
        val process = ProcessBuilder("/bin/sh", "-c", updater.uploadCommand)
                .redirectInput(archivePath.toFile())
                .redirectErrorStream(true)
                .start()
        try {
            process.waitFor(100, TimeUnit.MILLISECONDS)
        } finally {
            logIfNotEmpty(log::error, process.errorStream.readAllBytes())
            closeProcessAndStreams(process)
        }
    }
}
