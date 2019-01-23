package backupKotlin

import arrow.effects.IO
import arrow.effects.IO.Companion.unit
import com.github.salomonbrys.kodein.*
import mu.KotlinLogging

private val appConfig: AppConfig = kodein.instance()
private val generator: ArchiveGenerator = kodein.instance()
private val backupPerformer: BackupPerformer = kodein.instance()
private val log = KotlinLogging.logger {  }

fun main() {
    generateArchives()
            .flatMap { backupPerformer.backup() }
            .unsafeRunAsync { it.fold(
                    {exception -> throw exception},
                    {Unit}
            ) }
}

private fun generateArchives(): IO<Unit> {
    return appConfig.packageManagers
            .map(generator::generate)
            .fold(unit, runAsynchronously(log))
}
