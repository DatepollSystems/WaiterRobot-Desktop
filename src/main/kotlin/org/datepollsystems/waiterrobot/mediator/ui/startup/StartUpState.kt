package org.datepollsystems.waiterrobot.mediator.ui.startup

import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State

data class StartUpState(
    override val screenState: ScreenState = ScreenState.Loading,
    val showUpdateAvailable: Boolean = false
) : State<StartUpState> {
    override fun withScreenState(screenState: ScreenState): StartUpState = copy(screenState = screenState)
}