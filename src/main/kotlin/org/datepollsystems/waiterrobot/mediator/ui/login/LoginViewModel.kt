package org.datepollsystems.waiterrobot.mediator.ui.login

import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.api.createClient
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.app.removeLoginIdentifierEnvPrefix
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

class LoginViewModel(navigator: Navigator, viewModelScope: CoroutineScope) :
    ViewModel<LoginState>(navigator, viewModelScope, LoginState()) {

    private val authClient: HttpClient by lazy { createClient() }

    fun doLogin(email: String, password: String) = inVmScope {
        // TODO wrong password handling
        reduce { copy(screenState = ScreenState.Loading, loginErrorMessage = null) }

        App.config = Config.getFromLoginIdentifier(email)
        val authApi = AuthApi(authClient)

        try {
            val tokens = authApi.login(email.removeLoginIdentifierEnvPrefix(), password)
            Settings.accessToken = tokens.accessToken
            Settings.refreshToken = tokens.refreshToken!!

            navigator.navigate(Screen.ConfigurePrintersScreen)
        } catch (e: Exception) {
            println(e.stackTraceToString()) // TODO logger
            reduce { copy(screenState = ScreenState.Idle, loginErrorMessage = "Wrong credentials. Please try again!") }
        }
    }
}