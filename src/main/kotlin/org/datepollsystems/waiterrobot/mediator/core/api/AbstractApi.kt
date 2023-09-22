package org.datepollsystems.waiterrobot.mediator.core.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

// baseUrlLoader lambda is needed as the App.config can change after the class was already created by koin
// TODO refactor! (e.g. reinitialize the whole koin module when App.config changes)
abstract class AbstractApi(private val baseUrlLoader: () -> String, protected val client: HttpClient) {

    // Make sure that the baseUrl ends with a "/"
    private val baseUrl: String
        get() = baseUrlLoader().let {
            if (it.endsWith("/")) it else "$it/"
        }

    /**
     * prepend string (endpoint) with base and make sure that endpoint does not start with "/"
     */
    protected fun String.toFullUrl() = (baseUrl + this.removePrefix("/")).removeSuffix("/")

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

    protected suspend inline fun <reified B : RequestBodyDto> post(
        endpoint: String = "",
        body: B? = null,
        noinline block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse = client.post(endpoint.toFullUrl()) {
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        block?.invoke(this)
    }

    protected suspend fun head(
        endpoint: String = "",
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse = client.head(endpoint.toFullUrl()) {
        block?.invoke(this)
    }
}

// Marker interface
interface RequestBodyDto
