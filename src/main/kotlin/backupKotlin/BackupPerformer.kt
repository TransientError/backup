package backupKotlin

import com.google.common.annotations.VisibleForTesting
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.time.Instant
import java.time.Period

private val logger = KotlinLogging.logger {}
open class BackupPerformer(private val appConfig: AppConfig, private val storageServiceSelector: StorageServiceSelector) {
    fun backup() {
        val (toUpload, toLog) = appConfig.packageManagers
                .map(::getArchivePath)
                .partition { hasBeenModifiedInLast(1, it) }
        toLog.forEach { logger.info {"$it has not changed so we're not uploading it"} }
        toUpload.forEach(::uploadToServices)
    }

    @VisibleForTesting
    protected open fun hasBeenModifiedInLast(days: Int, file: Path): Boolean =
            Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS).toInstant()
                    .isAfter(Instant.now().minus(Period.ofDays(days)))

    private fun uploadToServices(path: Path) =
            storageServiceSelector.select().forEach { (updater, uploader) -> uploader.upload(updater, path)}
}