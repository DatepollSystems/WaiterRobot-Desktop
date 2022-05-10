package org.datepollsystems.waiterrobot.mediator.ui.main

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

class MainScreenViewModel(navigator: Navigator, viewModelScope: CoroutineScope) :
    ViewModel<MainScreenState>(navigator, viewModelScope, MainScreenState()) {
    fun logOut() {
        System.setProperty("accessToken", "")
        System.setProperty("sessionToken", "")

        navigator.navigate(Screen.LoginScreen)
    }
}
