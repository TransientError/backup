package backupKotlin

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

private class MockedConfigReader(objectMapper: ObjectMapper, configurationPath: String)
    : ConfigReader(objectMapper, configurationPath) {
    var stubbedResponse = true
    override fun fileExists(pathString: String): Boolean {
        return stubbedResponse
    }

    fun stubResponse(boolean: Boolean) {
        stubbedResponse = boolean
    }
}

class ConfigReaderTest {
    private val objectMapper = mock<ObjectMapper> {
        on { readValue(any<File>(), eq(AppConfig::class.java)) }doReturn
                AppConfig(listOf(PackageManager("name")))
    }
    private val configurationPath = "path"
    private var response: AppConfig = AppConfig()
    private val configReader = MockedConfigReader(objectMapper, configurationPath)

    @Test
    fun readConfigIfExists() {
        response = configReader.readConfig()

        assertThat(response).hasFieldOrPropertyWithValue("packageManagers", listOf(PackageManager("name")))
    }

    @Test
    fun dontReadConfigIfNotExists() {
        configReader.stubResponse(false)

        response = configReader.readConfig()

        assertThat(response).isEqualTo(AppConfig())
    }
}
