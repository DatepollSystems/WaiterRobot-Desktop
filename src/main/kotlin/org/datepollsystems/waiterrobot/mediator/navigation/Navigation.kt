package org.datepollsystems.waiterrobot.mediator.navigation

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import org.datepollsystems.waiterrobot.mediator.ui.login.LoginScreen

@Composable
fun Navigation() {
    val navigator = remember { Navigator(Screen.LoginScreen) }

    val screenState = navigator.screenState.collectAsState().value
    when (screenState) {
        Screen.LoginScreen -> {
            LoginScreen(navigator)
        }
        is Screen.StartScreen -> {
            StartScreen(navigator, screenState.name)
        }
    }.let {} // Force exhaustive
}

// TODO move to own file
@Composable
fun StartScreen(navigator: Navigator, name: String) {
    Text("Hello, $name!")
}