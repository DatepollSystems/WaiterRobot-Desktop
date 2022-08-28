package org.datepollsystems.waiterrobot.mediator.navigation

sealed class Screen {
    object StartUpScreen : Screen()
    object LoginScreen : Screen()
    object ConfigurePrintersScreen : Screen()
    object MainScreen : Screen()
}