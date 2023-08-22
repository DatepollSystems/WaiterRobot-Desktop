package org.datepollsystems.waiterrobot.mediator.ui.login

import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State

data class LoginState(
    override val screenState: ScreenState = ScreenState.Idle,
    val loginErrorMessage: String? = null
) : State<LoginState> {
    override fun withScreenState(screenState: ScreenState): LoginState = copy(screenState = screenState)
}
