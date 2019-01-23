package backupKotlin

import com.github.salomonbrys.kodein.*

private val appConfig: AppConfig = kodein.instance()
private val generator: ArchiveGenerator = kodein.instance()
private val backupPerformer: BackupPerformer = kodein.instance()

fun main(args: Array<String>) {
    generateArchives()
    backupPerformer.backup()
}

private fun generateArchives() {
    appConfig.packageManagers.forEach(generator::generate)
}
