package org.datepollsystems.waiterrobot.mediator.navigation

sealed class Screen {
    object LoginScreen : Screen()
    data class StartScreen(val name: String) : Screen()
}
