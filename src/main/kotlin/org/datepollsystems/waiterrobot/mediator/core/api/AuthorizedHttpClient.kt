package org.datepollsystems.waiterrobot.mediator.core.api

import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi


fun createAuthorizedClient(enableNetworkLogs: Boolean = false): AuthorizedClient = AuthorizedClient(
    createClient(enableNetworkLogs).config {
        configureAuth()
    }
)

class AuthorizedClient(val delegate: HttpClient)

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