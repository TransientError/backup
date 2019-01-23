package backupKotlin.StorageServices

import arrow.core.Try
import arrow.effects.IO
import backupKotlin.Updater
import backupKotlin.closeProcessStreams
import backupKotlin.logIfNotEmpty
import mu.KotlinLogging
import java.nio.file.Path
import java.util.concurrent.TimeUnit

interface StorageService {
    fun upload(updater: Updater, archivePath: Path): IO<Unit>
}

val log = KotlinLogging.logger {  }
class CustomStorageService : StorageService {

    private fun sweepInput(updater: Updater) {
        Try {
            require(updater.name.isNotBlank() && !updater.uploadCommand.isNullOrBlank())
        }.fold({log.error(it) {  }; throw it}, {Unit})
    }

    override fun upload(updater: Updater, archivePath: Path): IO<Unit> {
        sweepInput(updater)

        return IO<Process> {
            val command = "cat $archivePath | ${updater.uploadCommand}"
            log.info { "Executing: $command" }
            val process = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", command))
            process.waitFor(100, TimeUnit.MILLISECONDS)
            logIfNotEmpty(log::error, process.errorStream.readAllBytes())
            process
        }.map(::closeProcessStreams)
    }
}
