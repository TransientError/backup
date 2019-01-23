package backupKotlin

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private val log = KotlinLogging.logger {  }
open class ConfigReader(private val objectMapper: ObjectMapper, private val configurationPath: String) {
    fun readConfig() : AppConfig {
        return if (fileExists(configurationPath)) {
            objectMapper.readValue(File(configurationPath), AppConfig::class.java)
        } else {
            log.warn { "No config was found, loading empty AppConfig" }
            AppConfig()
        }
    }

    protected open fun fileExists(pathString: String) : Boolean {
        return Files.exists(Paths.get(configurationPath))
    }
}
