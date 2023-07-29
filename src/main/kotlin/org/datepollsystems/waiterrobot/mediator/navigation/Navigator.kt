package org.datepollsystems.waiterrobot.mediator.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.di.injectLoggerForClass
import org.koin.core.component.KoinComponent

/**
 * A really simple navigator
 * Attention does currently not support back navigation and state-keeping (each screen state gets discharged when navigated away)
 */
class Navigator(startScreen: Screen) : KoinComponent {
    private val logger by injectLoggerForClass()
    private val screenStateFlow: MutableStateFlow<Screen> = MutableStateFlow(startScreen)
    val screenState: StateFlow<Screen>
        get() = screenStateFlow

    private val scope = CoroutineScope(SupervisorJob())

    init {
        App.addLogoutListener { navigate(Screen.LoginScreen) }
    }

    fun navigate(screen: Screen) {
        logger.d("Navigate to: $screen")
        scope.launch {
            screenStateFlow.emit(screen)
        }
    }
}