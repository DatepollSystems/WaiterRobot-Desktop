package org.datepollsystems.waiterrobot.mediator.ui.startup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.mediator.core.EmptyState
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen
import org.datepollsystems.waiterrobot.mediator.utils.emptyToNull

class StartUpViewModel(navigator: Navigator, viewModelScope: CoroutineScope) :
    ViewModel<EmptyState>(navigator, viewModelScope, EmptyState) {

    init {
        viewModelScope.launch {
            delay(1500)
            val startScreen = if (System.getProperty("sessionToken", null).emptyToNull() != null) {
                Screen.LoginScreen
            } else {
                Screen.MainScreen("You were already logged in!")
            }
            navigator.navigate(startScreen)
        }
    }
}