package org.datepollsystems.waiterrobot.mediator.core.api

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.data.api.ApiException

fun HttpClientConfig<*>.installApiClientExceptionTransformer(json: Json, logger: Logger) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            val clientException =
                exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest

            // Get as string and do custom serialization here, so we can fallback to a generic error
            // with the basic error information if the client does not know the codeName.
            val jsonString = clientException.response.bodyAsText()
            throw try {
                json.decodeFromString<ApiException>(jsonString)
            } catch (e: SerializationException) {
                logger.w(e) { "Could not serialize ClientError using fallback" }
                try {
                    json.decodeFromString<ApiException.Generic>(jsonString)
                } catch (_: SerializationException) {
                    logger.e(e) { "Fallback ClientError Serialization failed" }
                    ApiException.Generic(
                        message = "Unknown error",
                        httpCode = exception.response.status.value,
                        codeName = "UNKNOWN"
                    )
                }
            }
        }
    }
}