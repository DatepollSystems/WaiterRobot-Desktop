package org.datepollsystems.waiterrobot.mediator.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Navigator(startScreen: Screen) {
    private val screenStateFlow: MutableStateFlow<Screen> = MutableStateFlow(startScreen)
    val screenState: StateFlow<Screen>
        get() = screenStateFlow

    fun navigate(screen: Screen) {
        println("Navigate to: $screen")
        screenStateFlow.value = screen
    }
}