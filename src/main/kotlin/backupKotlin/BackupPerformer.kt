package backupKotlin

import com.google.common.annotations.VisibleForTesting
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.time.Instant
import java.time.Period
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}
open class BackupPerformer(private val appConfig: AppConfig, private val storageServiceSelector: StorageServiceSelector) {
    fun backup() {
        val (toUpload, toLog) = appConfig.packageManagers
                .map(::getArchivePath)
                .partition { hasBeenModifiedInLast(1, it) }
        toLog.forEach { logger.info {"$it has not changed so we're not uploading it"} }
        return toUpload.forEach{ path ->  runIndependently({ uploadToServices(path) },  logger) }
    }

    @VisibleForTesting
    protected open fun hasBeenModifiedInLast(days: Int, file: Path): Boolean =
            if (!file.toFile().exists()) {
                logger.warn {"$file archive does not exist"}
                false
            } else {
                Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS).toInstant()
                        .isAfter(Instant.now().minus(Period.ofDays(days)))
            }

    private fun uploadToServices(path: Path) =
            storageServiceSelector.select()
                    .forEach { (updater, storageService) ->  storageService.upload(updater, path)  }
}