package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

abstract class AbstractApi(baseUrl: String) {
    protected abstract val client: HttpClient

    // Make sure that the baseUrl ends with a "/"
    private val baseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    /**
     * prepend string (endpoint) with base and make sure that endpoint does not start with "/"
     */
    private fun String.toFullUrl() = (baseUrl + this.removePrefix("/")).removeSuffix("/")

    protected suspend fun get(
        endpoint: String = "",
        vararg query: Pair<String, Any>,
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse = client.get(endpoint.toFullUrl()) {
        query.forEach {
            url.parameters.append(it.first, it.second.toString())
        }

        block?.invoke(this)
    }

    protected suspend fun post(
        endpoint: String = "",
        body: RequestBodyDto? = null,
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse = client.post(endpoint.toFullUrl()) {
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        block?.invoke(this)
    }
}

// Marker interface
interface RequestBodyDto