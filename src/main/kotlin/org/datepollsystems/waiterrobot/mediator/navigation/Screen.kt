package org.datepollsystems.waiterrobot.mediator.navigation

import org.datepollsystems.waiterrobot.mediator.app.MediatorConfiguration

sealed class Screen {
    object StartUpScreen : Screen()
    object LoginScreen : Screen()
    object ConfigurePrintersScreen : Screen()
    data class MainScreen(val config: MediatorConfiguration) : Screen()
}
