package org.datepollsystems.waiterrobot.mediator.core.api

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.AppVersion

fun createClient(enableNetworkLogs: Boolean = App.config.enableNetworkLogging, logger: Logger) = HttpClient {
    val json = Json {
        ignoreUnknownKeys = true
    }

    install(ContentNegotiation) {
        json(json)
    }

    install(HttpTimeout) {
        @Suppress("MagicNumber")
        requestTimeoutMillis = 5000 // TODO increase?
    }

    defaultRequest {
        header("X-App-Version", AppVersion.current.toString())
        header("X-App-Os", System.getProperty("os.name"))
        header("X-App-Name", "desktop")
    }

    if (enableNetworkLogs) {
        install(Logging) {
            this.logger = CustomKtorLogger(logger.tag)
            this.level = LogLevel.ALL
        }
    }

    installApiClientExceptionTransformer(json, logger)
}
