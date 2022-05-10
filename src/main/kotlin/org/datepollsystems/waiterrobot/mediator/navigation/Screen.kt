package org.datepollsystems.waiterrobot.mediator.navigation

sealed class Screen {
    object StartUpScreen : Screen()
    object LoginScreen : Screen()
    data class MainScreen(val text: String) : Screen()
}
