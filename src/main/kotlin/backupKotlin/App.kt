package backupKotlin

import com.github.salomonbrys.kodein.*
import com.google.common.base.Throwables
import mu.KotlinLogging
import java.lang.Exception
import kotlin.concurrent.thread

private val appConfig: AppConfig = kodein.instance()
private val generator: ArchiveGenerator = kodein.instance()
private val backupPerformer: BackupPerformer = kodein.instance()
private val log = KotlinLogging.logger {  }

fun main() {
    generateArchives()
    backupPerformer.backup()
}

private fun generateArchives() {
    try {
        return appConfig.packageManagers
                .forEach { packageManager -> generator.generate(packageManager)  }
    } catch (e: Exception) {
        log.warn(e) { Throwables.getStackTraceAsString(e) }
    }
}
