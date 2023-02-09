package org.datepollsystems.waiterrobot.mediator.ui.login

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

class LoginViewModel(navigator: Navigator, viewModelScope: CoroutineScope, private val authApi: AuthApi) :
    ViewModel<LoginState>(navigator, viewModelScope, LoginState()) {

    fun doLogin(email: String, password: String) = inVmScope {
        // TODO wrong password handling
        reduce { copy(screenState = ScreenState.Loading, loginErrorMessage = null) }

        try {
            val tokens = authApi.login(email, password)
            Settings.accessToken = tokens.accessToken
            Settings.refreshToken = tokens.refreshToken!!

            navigator.navigate(Screen.ConfigurePrintersScreen)
        } catch (e: Exception) {
            reduce { copy(screenState = ScreenState.Idle, loginErrorMessage = "Wrong credentials. Please try again!") }
        }
    }
}