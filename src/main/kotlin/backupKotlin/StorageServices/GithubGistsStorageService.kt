package backupKotlin.StorageServices

import arrow.effects.IO
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

    override fun upload(updater: Updater, archivePath: Path): IO<Unit> {
        sweepInput(updater)
        return IO {
            retryableHttpClient.submitWithRetries { submitEditGistRequest(updater, archivePath) }
        }.map { Unit }
    }

    private fun sweepInput(updater: Updater) =
            require(!(updater.credentials.isNullOrBlank() || updater.destination.isNullOrBlank()))
            {"Github gists require an OAuth token and a gist id"}

    private fun submitEditGistRequest(updater: Updater, archivePath: Path)
            : Triple<Request, Response, Result<ByteArray, FuelError>> =
            Fuel.patch("https://api.github.com/gists/${updater.destination}")
                .header(HttpHeaders.AUTHORIZATION to "token ${updater.credentials}")
                .jsonBody(generateEditGistsBody(archivePath))
                .response()

    @VisibleForTesting
    internal fun generateEditGistsBody(archivePath: Path): String {
        return JSONObject(mapOf(
                GITHUB_DESCRIPTION_KEY to "backup for TransientError's dotfiles/packages",
                GITHUB_FILES_KEY to JSONObject(mapOf(
                        archivePath.fileName to JSONObject(mapOf(
                                GITHUB_CONTENT_KEY to fileReader.readFiles(archivePath)
                        ))
                ))
        )).toString()
    }

}