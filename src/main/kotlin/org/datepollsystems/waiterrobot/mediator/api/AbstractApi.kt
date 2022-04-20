package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.datepollsystems.waiterrobot.mediator.api.dto.Sendable

abstract class AbstractApi(baseUrl: String) {
    protected abstract val client: HttpClient

    // Make sure that the baseUrl ends with a "/"
    private val baseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    /**
     * prepend string (endpoint) with base and make sure that endpoint does not start with "/"
     */
    protected fun String.toFullUrl() = baseUrl + this.removePrefix("/")

    protected suspend inline fun <reified T : Any> get(
        endpoint: String,
        query: Map<String, String>? = null,
        noinline block: (HttpRequestBuilder.() -> Unit)? = null
    ): T {
        val queryString = query?.entries?.joinToString("&", "?") { "${it.key}=${it.value}" } ?: ""
        val response = client.get(endpoint.toFullUrl().plus(queryString)) {
            block?.invoke(this)
        }

        return response.body()
    }

    protected suspend inline fun <reified T : Any> post(
        endpoint: String,
        body: Sendable? = null,
        noinline block: (HttpRequestBuilder.() -> Unit)? = null
    ): T {
        return post(endpoint, body, block).body()
    }

    @JvmName("postForResponse")
    suspend fun post(
        endpoint: String,
        body: Sendable? = null,
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse {
        return client.post(endpoint.toFullUrl()) {
            contentType(ContentType.Application.Json)
            setBody(body)
            block?.invoke(this)
        }
    }
}