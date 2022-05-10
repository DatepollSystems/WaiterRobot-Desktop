package org.datepollsystems.waiterrobot.mediator.ui.main

import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.core.State

data class MainScreenState(override val screenState: ScreenState = ScreenState.Idle) : State
