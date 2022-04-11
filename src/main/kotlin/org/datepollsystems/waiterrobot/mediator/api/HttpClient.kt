package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createClient(enableNetworkLogs: Boolean = false) = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 5000
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
    install(Auth) {
        suspend fun refreshTokens(sessionToken: String): BearerTokens? {
            val authApi = AuthApi(createClient())
            return try {
                val tokenInfo = authApi.refresh(sessionToken)

                System.setProperty("accessToken", tokenInfo.accessToken)
                tokenInfo.sessionToken?.let {
                    System.setProperty(
                        "sessionToken",
                        it
                    )
                } // Only override when got a new sessionToken

                BearerTokens(
                    accessToken = tokenInfo.accessToken,
                    refreshToken = sessionToken
                )
            } catch (e: Exception) {
                // TODO improve request errors handling (-> try again, logout?, no connection info)
                //  logging and logout
                null
            }
        }

        bearer {
            // TODO check on startup if access and session token are there, otherwise logout
            loadTokens {
                // TODO use other storage?
                val accessToken: String? = System.getProperty("accessToken", null)
                val sessionToken: String? = System.getProperty("sessionToken", null)

                return@loadTokens when {
                    // TODO logout when no sessionToken found
                    sessionToken == null -> throw java.lang.IllegalStateException("No session token saved")
                    accessToken == null -> refreshTokens(sessionToken)
                    else -> BearerTokens(accessToken, sessionToken)
                }
            }

            refreshTokens {
                val sessionToken: String = System.getProperty("sessionToken", null)
                    ?: throw IllegalStateException("No session token stored")

                return@refreshTokens refreshTokens(sessionToken)
            }
        }
    }
}

