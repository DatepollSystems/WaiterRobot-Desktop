package org.datepollsystems.waiterrobot.mediator.ui.login

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

@Composable
fun LoginScreen(navigator: Navigator) {
    Button(onClick = {
        navigator.navigate(Screen.StartScreen("Some test parameter"))
    }) {
        Text("Say Hello")
    }
}