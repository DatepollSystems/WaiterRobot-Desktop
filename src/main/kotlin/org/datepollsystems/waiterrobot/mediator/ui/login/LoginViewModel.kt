package org.datepollsystems.waiterrobot.mediator.ui.login

import io.ktor.client.*
import io.ktor.http.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.app.removeLoginIdentifierEnvPrefix
import org.datepollsystems.waiterrobot.mediator.core.AbstractViewModel
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.api.createClient
import org.datepollsystems.waiterrobot.mediator.data.api.ApiException
import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

class LoginViewModel(navigator: Navigator) :
    AbstractViewModel<LoginState>(navigator, LoginState()) {

    private val authClient: HttpClient by lazy { createClient() }

    fun doLogin(email: String, password: String) = inVmScope {
        reduce { copy(screenState = ScreenState.Loading, loginErrorMessage = null) }

        App.config = Config.getFromLoginIdentifier(email)
        Settings.loginPrefix = App.config.loginPrefix
        val authApi = AuthApi(authClient)

        try {
            val tokens = authApi.login(email.removeLoginIdentifierEnvPrefix(), password)
            Settings.accessToken = tokens.accessToken
            Settings.refreshToken = tokens.refreshToken!!

            navigator.navigate(Screen.ConfigurePrintersScreen)
        } catch (e: Exception) {
            // TODO this should be unified (see WR-307)
            if (e is ApiException.Unauthorized || e is ApiException.CredentialsIncorrect ||
                (e is ApiException && e.httpCode == HttpStatusCode.Unauthorized.value)
            ) {
                println(e.stackTraceToString()) // TODO logger
                reduce {
                    copy(
                        screenState = ScreenState.Idle,
                        loginErrorMessage = "Wrong credentials. Please try again!"
                    )
                }
            } else {
                throw e
            }
        }
    }
}