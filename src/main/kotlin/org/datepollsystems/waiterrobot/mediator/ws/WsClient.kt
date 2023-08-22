package org.datepollsystems.waiterrobot.mediator.ws

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.api.CustomKtorLogger
import org.datepollsystems.waiterrobot.mediator.core.api.configureAuth
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import kotlin.time.Duration.Companion.seconds

typealias WsMessageHandler<T> = suspend (AbstractWsMessage<T>) -> Unit

fun createWsClient(enableNetworkLogs: Boolean = App.config.enableNetworkLogging, logger: Logger) = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
        pingInterval = 10.seconds.inWholeMilliseconds // TODO adapt?
        // Use compression? (especially for pdf transferring as internet connection may be very slow)
        // TODO test if it really brings benefits
        // TODO receiving does not work out of the box probably needs adaptions on the backend
        //  (how to tell spring to use compression for responses?)
        //  Maybe we should then also add compressMinSize as a header so the server also respects this #
        //  or how does ktor determine if it must decompress incoming messages
        /*extensions {
            install(WebSocketDeflateExtension) {
                compressIfBiggerThan(500) // Do not compress very small messages as this would probably make them bigger
                // TODO some benchmarking needed for which minSize makes most sense for our type of data
            }
        }*/
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10.seconds.inWholeMilliseconds
    }
    configureAuth(enableNetworkLogs, logger)
    if (enableNetworkLogs) {
        install(Logging) {
            this.logger = CustomKtorLogger(logger.tag)
            this.level = LogLevel.ALL
        }
    }
}
