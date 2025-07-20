package org.datepollsystems.waiterrobot.mediator.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Config

@Composable
fun SelectedEnvironmentInfo() {
    if (App.config == Config.Prod) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Yellow.copy(alpha = 0.9f),
        contentColor = Color.Black
    ) {
        Text(
            text = App.config.displayName,
            modifier = Modifier.padding(5.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
