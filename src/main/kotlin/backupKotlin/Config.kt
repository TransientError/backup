package backupKotlin

import backupKotlin.StorageServices.FileReader
import backupKotlin.StorageServices.GithubGistStorageService
import backupKotlin.StorageServices.StorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.salomonbrys.kodein.*

val kodein = Kodein {
    constant("configurationPath") with "./backup.json"
    constant("charset") with Charsets.UTF_8
    constant("maxRetries") with 3
    bind<ObjectMapper>() with singleton { jacksonObjectMapper() }
    bind<AppConfig>() with singleton {
        kodein.newInstance { ConfigReader(instance(), instance("configurationPath")).readConfig() }
    }
    bind<ArchiveGenerator>() with singleton { ArchiveGenerator(instance()) }
    bind<StorageServiceSelector>() with singleton { kodein.newInstance { StorageServiceSelector(instance()) } }
    bind<BackupPerformer>() with singleton { kodein.newInstance { BackupPerformer(instance(), instance()) } }
    bind<FileReader>() with singleton { kodein.newInstance { FileReader(instance("charset")) } }
    bind<RetryableHttpClient>() with singleton {kodein.newInstance { RetryableHttpClient(instance("maxRetries")) }}
    bind<StorageService>("github-gists") with singleton {
        kodein.newInstance { GithubGistStorageService(instance(), instance()) }
    }
}
