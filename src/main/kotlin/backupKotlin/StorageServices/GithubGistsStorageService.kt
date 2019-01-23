package backupKotlin.StorageServices

import backupKotlin.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.common.annotations.VisibleForTesting
import com.google.common.io.Files
import com.google.common.net.HttpHeaders
import org.json.JSONObject
import java.nio.file.Path

const val GITHUB_DESCRIPTION_KEY = "description"
const val GITHUB_FILES_KEY = "files"
const val GITHUB_CONTENT_KEY = "content"

internal open class GithubGistStorageService(
        private val fileReader: FileReader,
        private val retryableHttpClient: RetryableHttpClient) : StorageService {
    lateinit var archivePath: Path
    lateinit var updater: Updater

    override fun upload(updater: Updater, archivePath: Path) {
        this.archivePath = archivePath
        this.updater = updater
        sweepInput(updater)
        retryableHttpClient.submitWithRetries { submitEditGistRequest() }
    }

    private fun sweepInput(updater: Updater) =
            require(!(updater.credentials.isNullOrBlank() || updater.destination.isNullOrBlank()))
            {"Github gists require an OAuth token and a gist id"}

    private fun submitEditGistRequest(): Triple<Request, Response, Result<ByteArray, FuelError>> =
            Fuel.patch("https://api.github.com/gists/${updater.destination}")
                .header(HttpHeaders.AUTHORIZATION to "token ${updater.credentials}")
                .jsonBody(generateEditGistsBody())
                .response()

    @VisibleForTesting
    internal fun generateEditGistsBody(): String =
            JSONObject(mapOf(
                GITHUB_DESCRIPTION_KEY to getDescriptionForPath(),
                GITHUB_FILES_KEY to JSONObject(mapOf(
                        archivePath.fileName to JSONObject(mapOf(
                                GITHUB_CONTENT_KEY to fileReader.readFiles(archivePath)
                        ))
                ))
            )).toString()

    private fun getDescriptionForPath(): String = "backup for ${getNameForArchive()}"

    private fun getNameForArchive() : String =
            Files.getNameWithoutExtension(archivePath.toString()).split("-").first()

}