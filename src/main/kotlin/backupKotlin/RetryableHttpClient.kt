package backupKotlin

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.common.collect.Range
import com.google.common.util.concurrent.Uninterruptibles
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}
class RetryableHttpClient(private val maxRetries: Int) {

    fun <T : Any> submitWithRetries(fuelRequest : () -> Triple<Request, Response, Result<T, FuelError>>)
            : Triple<Request, Response, Result<T, FuelError>> {
        var tries = 0
        val (request, response, result) = fuelRequest.invoke()

        while (!Range.openClosed(200, 299).contains(response.statusCode) && tries < maxRetries) {
            logger.warn("http call failed with status code: ${response.statusCode}" +
                    " message: ${response.responseMessage}")
            ++tries
            Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS)
        }

        if (tries == maxRetries) {
            when (result) {
                is Result.Failure -> logger.error("http request failed $tries times", result.getException())
                is Result.Success -> return Triple(request, response, result)
            }
        }

        return Triple(request, response, result)
    }

}