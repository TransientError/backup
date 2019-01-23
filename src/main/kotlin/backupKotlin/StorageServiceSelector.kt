package backupKotlin

import backupKotlin.StorageServices.CustomStorageService
import backupKotlin.StorageServices.StorageService
import com.github.salomonbrys.kodein.instance

private val KNOWN_UPDATERS = mapOf<UpdaterName, StorageService>("github-gists" to kodein.instance("github-gists"))

class StorageServiceSelector(private val appConfig: AppConfig) {
    fun select(): Map<Updater, StorageService> = appConfig.updaters
            .associate { it to KNOWN_UPDATERS.getOrDefault(it.name, CustomStorageService()) }
}
