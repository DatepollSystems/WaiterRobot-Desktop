package org.datepollsystems.waiterrobot.mediator.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.datepollsystems.waiterrobot.mediator.core.ScreenState
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.Res
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.ok
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorDialog(error: ScreenState.Error) {
    AlertDialog(
        onDismissRequest = error.onDismiss,
        confirmButton = {
            Button(onClick = error.onDismiss) {
                Text(text = stringResource(Res.string.ok))
            }
        },
        title = {
            Text(text = stringResource(error.title))
        },
        text = {
            Text(text = stringResource(error.message))
        }
    )
}
