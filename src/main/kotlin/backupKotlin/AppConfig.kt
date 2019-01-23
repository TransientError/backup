package backupKotlin

typealias PackageManagerName = String
typealias ListGenerator = String

data class PackageManager(val name: PackageManagerName = "", var listGenerator: ListGenerator? = null)

data class AppConfig(val packageManagers: List<PackageManager> = emptyList(), val updaters: List<Updater> = emptyList())

typealias UpdaterName = String
data class Updater(
        val name: UpdaterName,
        val uploadCommand: String? = null,
        val credentials: String? = null,
        val destination: String? = null)
