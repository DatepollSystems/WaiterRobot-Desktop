package org.datepollsystems.waiterrobot.mediator.ui.main

import kotlinx.coroutines.CoroutineScope
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ViewModel
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator

class MainScreenViewModel(navigator: Navigator, viewModelScope: CoroutineScope) :
    ViewModel<MainScreenState>(navigator, viewModelScope, MainScreenState()) {
    fun logOut() {
        App.logout()
    }
}
