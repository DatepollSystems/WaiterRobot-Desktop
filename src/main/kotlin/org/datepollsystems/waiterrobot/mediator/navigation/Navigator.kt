package org.datepollsystems.waiterrobot.mediator.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.datepollsystems.waiterrobot.mediator.App

/**
 * A really simple navigator
 * Attention does currently not support back navigation and state-keeping (each screen state gets discharged when navigated away)
 */
class Navigator(startScreen: Screen) {
    private val screenStateFlow: MutableStateFlow<Screen> = MutableStateFlow(startScreen)
    val screenState: StateFlow<Screen>
        get() = screenStateFlow

    init {
        App.addLogoutListener { navigate(Screen.LoginScreen) }
    }

    fun navigate(screen: Screen) {
        println("Navigate to: $screen") // TODO logger
        screenStateFlow.value = screen
    }
}