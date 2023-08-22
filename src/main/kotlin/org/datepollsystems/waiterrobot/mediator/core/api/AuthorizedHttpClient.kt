package org.datepollsystems.waiterrobot.mediator.core.api

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi

fun createAuthorizedClient(enableNetworkLogs: Boolean = false, logger: Logger): AuthorizedClient = AuthorizedClient(
    createClient(enableNetworkLogs, logger).config {
        configureAuth(enableNetworkLogs, logger)
    }
)

class AuthorizedClient(val delegate: HttpClient)

fun HttpClientConfig<*>.configureAuth(enableNetworkLogs: Boolean, logger: Logger) {
    install(Auth) {
        suspend fun refreshTokens(refreshToken: String): BearerTokens? {
            val authApi = AuthApi(createClient(enableNetworkLogs, logger))
            @Suppress("TooGenericExceptionCaught")
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
                logger.e(e) { "Error while refreshing token: ${e.message}" }
                null
            }
        }

        bearer {
            loadTokens {
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
                val sessionToken: String = Settings.refreshToken ?: error("No session token stored")

                return@refreshTokens refreshTokens(sessionToken)
            }
        }
    }
}
