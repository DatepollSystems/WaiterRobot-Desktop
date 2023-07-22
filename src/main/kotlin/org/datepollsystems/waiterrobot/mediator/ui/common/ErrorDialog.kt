package org.datepollsystems.waiterrobot.mediator.ui.common

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.datepollsystems.waiterrobot.mediator.core.ScreenState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ErrorDialog(error: ScreenState.Error) {
    AlertDialog(
        onDismissRequest = error.onDismiss,
        confirmButton = {
            Button(onClick = error.onDismiss) {
                Text("OK")
            }
        },
        title = {
            Text(text = error.title)
        },
        text = {
            Text(text = error.message)
        }
    )
}
