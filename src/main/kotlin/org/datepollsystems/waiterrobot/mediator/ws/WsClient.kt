package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.api.configureAuth
import org.datepollsystems.waiterrobot.mediator.api.createClient
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.ws.messages.AbstractWsMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessageResponse

typealias WsMessageHandler<T> = suspend (AbstractWsMessage<T>) -> Unit

fun createWsClient(enableNetworkLogs: Boolean = false) = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
        pingInterval = 1_000 // TODO adapt?
        // Use compression (improve network usage especially for pdf transferring as internet connection may be very slow)
        // TODO test if it really brings benefits
        // TODO receiving does not work out of the box probably needs adaptions on the backend (how to tell spring to use compression for responses?)
        //  Maybe we should then also add compressMinSize as a header so the server also respects this or how does ktor determine if it must decompress incoming messages
        /*extensions {
            install(WebSocketDeflateExtension) {
                compressIfBiggerThan(500) // Do not compress very small messages as this would probably make them bigger
                // TODO some benchmarking needed for which minSize makes most sense for our type of data
            }
        }*/
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10_000
        // TODO figure out what this actually means (seems not to "close" the connection after the specified time of inactivity, also when set very low)
        // socketTimeoutMillis = 3 * 60 * 1_000 // There should be a ping message every minute, so timeout if did not get multiple pings
    }
    configureAuth()
    if (enableNetworkLogs) {
        install(Logging) {
            // TODO use real logger
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.ALL
        }
    }
}

// WebSocket debug/test example
fun main(): Unit = runBlocking {
    // Login
    val tokens = AuthApi(createClient()).login("admin@admin.org", "admin")
    Settings.accessToken = tokens.accessToken
    Settings.refreshToken = tokens.refreshToken!!
    Settings.organisationId = 1L

    // Register a Handler for a specific websocket message
    App.socketManager.handle<HelloMessageResponse> {
        println("Handler for HelloMessageResponse called with: $it")

        if (it.body.text != "Hello second") {
            delay(2000)
            App.socketManager.send(HelloMessage(text = "second"))
        }
    }

    App.socketManager.addRegisterMessage(HelloMessage(text = "Test Register"))
    App.socketManager.send(HelloMessage(text = "Test send"))

    try {
        listOf(
            launch(CoroutineName("lauch1")) {
                repeat(15) {
                    println("I'm alive since $it sec.")
                    delay(1_000)
                }
            },
            launch(CoroutineName("lauch2")) {
                delay(10_000)
                App.socketManager.send(HelloMessage(text = "second"))
            },
            launch(CoroutineName("lauch3")) {
                delay(12_000)
                //App.socketManager.send(HelloMessage2(text = "test crash"))
                //App.socketManager.close()
            }
        ).joinAll() // Simulate some "application live time"
    } catch (e: Exception) {
        println(e)
    }
    println("Stopping client")
    App.socketManager.close()
    println("finished")
}