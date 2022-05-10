package org.datepollsystems.waiterrobot.mediator.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.datepollsystems.waiterrobot.mediator.navigation.Screen

@Composable
fun MainScreen(args: Screen.MainScreen, vm: MainScreenViewModel) {
    Column {
        Text(args.text)
        Button(onClick = vm::logOut) {
            Text("Logout")
        }
    }
}