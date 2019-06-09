package backupKotlin

import arrow.effects.IO
import backupKotlin.StorageServices.StorageService
import com.nhaarman.mockitokotlin2.*
import org.mockito.Mockito
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test

class MockBackupPerformer(appConfig: AppConfig, serviceSelector: StorageServiceSelector)
    : BackupPerformer(appConfig, serviceSelector) {
    var stubResponse = true

    fun setStubbedResponse(stubbed: Boolean) {
        stubResponse = stubbed
    }

    override fun hasBeenModifiedInLast(days: Int, file: Path): Boolean {
        return stubResponse
    }
}

class BackupPerformerTest {

    private val packageName = "packageName"
    private val packagePath = Paths.get(".", "archives", "$packageName-backup.txt")
    private val appConfig = AppConfig(listOf(PackageManager(packageName)))
    private val storageService = mock<StorageService> { }
    private val map = mapOf(Updater("name") to storageService)
    private val storageServiceSelector = mock<StorageServiceSelector> {
        on {select()} doReturn map
    }

    @Test
    fun shouldUploadIfFileHasBeenModified() {
        val backupPerformer = MockBackupPerformer(appConfig, storageServiceSelector)

        backupPerformer.backup()

        map.forEach {updater, uploader -> verify(uploader).upload(updater, packagePath)}
    }

    @Test
    fun shouldNotUploadIfFileHasNotBeenModified() {
        val backupPerformer = MockBackupPerformer(appConfig, storageServiceSelector)
        backupPerformer.setStubbedResponse(false)

        backupPerformer.backup()

        map.forEach {updater, uploader -> verify(uploader, never()).upload(updater, packagePath)}
    }

}