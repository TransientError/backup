package backupKotlin

import backupKotlin.StorageServices.*
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import java.nio.file.Paths

private val path = Paths.get(".", "archives", "sample-name.txt")
private val request = mock<Request>()
private val response = mock<Response>()
private val result = mock<Result<ByteArray, FuelError>>()
private val triple = Triple(request, response, result)
private val retryableHttpClient = mock<RetryableHttpClient> {
    on {submitWithRetries<ByteArray>(any())} doReturn triple
}
private val fileReader = mock<FileReader> {
    on {readFiles(path)} doReturn "content"
}

private val contentString = JSONObject(mapOf(path.fileName to
        JSONObject(mapOf(
                GITHUB_CONTENT_KEY to "content"
        )))).toString()

class GithubGistStorageServiceTest {

    @Test
    fun shouldSendEditRequest() {
        val updater = Updater("name", credentials = "creds", destination = "gist_id")
        val githubGistStorageService = GithubGistStorageService(fileReader, retryableHttpClient)
        githubGistStorageService.upload(updater, path)

        val json = JSONObject(githubGistStorageService.generateEditGistsBody())
        assertThat(json.getJSONObject(GITHUB_FILES_KEY).toString()).isEqualTo(contentString)
    }
}