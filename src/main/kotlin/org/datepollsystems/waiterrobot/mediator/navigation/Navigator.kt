package org.datepollsystems.waiterrobot.mediator.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A really simple navigator
 * Attention does currently not support back navigation and state-keeping (each screen state gets discharged when navigated away)
 */
class Navigator(startScreen: Screen) {
    private val screenStateFlow: MutableStateFlow<Screen> = MutableStateFlow(startScreen)
    val screenState: StateFlow<Screen>
        get() = screenStateFlow

    fun navigate(screen: Screen) {
        println("Navigate to: $screen")
        screenStateFlow.value = screen
    }
}