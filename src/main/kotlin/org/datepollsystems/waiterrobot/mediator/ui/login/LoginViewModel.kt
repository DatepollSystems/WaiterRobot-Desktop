package org.datepollsystems.waiterrobot.mediator.ui.login

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.api.AuthApi
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
            System.setProperty("accessToken", tokens.accessToken)
            System.setProperty("sessionToken", tokens.sessionToken!!)

            navigator.navigate(Screen.MainScreen("Logged in successfully")) // TODO
        } catch (e: Exception) {
            reduce { copy(screenState = ScreenState.Idle, loginErrorMessage = "Wrong credentials. Please try again!") }
        }
    }
}