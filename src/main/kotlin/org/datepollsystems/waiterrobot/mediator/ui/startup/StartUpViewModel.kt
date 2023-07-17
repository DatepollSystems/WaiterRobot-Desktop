package org.datepollsystems.waiterrobot.mediator.ui.startup

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.AbstractViewModel
import org.datepollsystems.waiterrobot.mediator.core.EmptyState
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

class StartUpViewModel(navigator: Navigator) :
    AbstractViewModel<EmptyState>(navigator, EmptyState) {

    init {
        viewModelScope.launch {
            delay(1500)
            val startScreen = if (Settings.refreshToken == null) {
                Screen.LoginScreen
            } else {
                Screen.ConfigurePrintersScreen
            }
            navigator.navigate(startScreen)
        }
    }
}