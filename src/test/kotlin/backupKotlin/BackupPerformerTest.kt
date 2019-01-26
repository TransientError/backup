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
    private val packagePath = Paths.get(".", "archives", "$packageName-package.txt")
    private val appConfig = AppConfig(listOf(PackageManager(packageName)))
    private val storageServiceSelector = Mockito.mock(StorageServiceSelector::class.java)!!
    private val storageService = mock<StorageService> {
        on {upload(Updater("name"), packagePath)} doReturn IO.unit
    }
    private val map = mapOf(Updater("name") to storageService)

    @Test
    fun shouldUploadIfFileHasBeenModified() {
        whenever(storageServiceSelector.select()).thenReturn(map)
        val backupPerformer = MockBackupPerformer(appConfig, storageServiceSelector)

        backupPerformer.backup()

        map.forEach {updater, uploader -> verify(uploader).upload(updater, packagePath)}
    }

    @Test
    fun shouldNotUploadIfFileHasNotBeenModified() {
        whenever(storageServiceSelector.select()).thenReturn(map)
        val backupPerformer = MockBackupPerformer(appConfig, storageServiceSelector)
        backupPerformer.setStubbedResponse(false)

        backupPerformer.backup()

        map.forEach {updater, uploader -> verify(uploader, never()).upload(updater, packagePath)}
    }

}