package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Settings

fun createClient(enableNetworkLogs: Boolean = App.config.enableNetworkLogging) = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 5000 // TODO increase?
    }
    if (enableNetworkLogs) {
        install(Logging) {
            // TODO use real logger
            logger = object : Logger {
                override fun log(message: String) {
                    println("KTOR: $message")
                }
            }
            level = LogLevel.ALL
        }
    }
}

fun createAuthenticatedClient(enableNetworkLogs: Boolean = false) = createClient(enableNetworkLogs).config {
    configureAuth()
}

fun HttpClientConfig<*>.configureAuth() {
    install(Auth) {
        suspend fun refreshTokens(refreshToken: String): BearerTokens? {
            val authApi = AuthApi(createClient())
            return try {
                val tokenInfo = authApi.refresh(refreshToken)

                Settings.accessToken = tokenInfo.accessToken
                // Only override when got a new sessionToken
                tokenInfo.refreshToken?.let { Settings.refreshToken = it }

                BearerTokens(
                    accessToken = tokenInfo.accessToken,
                    refreshToken = tokenInfo.refreshToken ?: refreshToken
                )
            } catch (e: Exception) {
                // TODO improve request errors handling (-> try again, logout?, no connection info)
                //  logging and logout
                println("Error while refreshing token: ${e.message}")
                e.printStackTrace()
                null
            }
        }

        bearer {
            loadTokens {
                // TODO use other storage?
                val accessToken: String? = Settings.accessToken
                val sessionToken: String? = Settings.refreshToken

                return@loadTokens when {
                    sessionToken == null -> {
                        App.logout()
                        null
                    }
                    accessToken == null -> refreshTokens(sessionToken)
                    else -> BearerTokens(accessToken, sessionToken)
                }
            }

            refreshTokens {
                val sessionToken: String = Settings.refreshToken
                    ?: throw IllegalStateException("No session token stored")

                return@refreshTokens refreshTokens(sessionToken)
            }
        }
    }
}