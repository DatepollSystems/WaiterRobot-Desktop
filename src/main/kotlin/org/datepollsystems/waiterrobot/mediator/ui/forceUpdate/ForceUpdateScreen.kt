package org.datepollsystems.waiterrobot.mediator.ui.forceUpdate

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ForceUpdateScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aktualisierung benötigt",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h2
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Deine installierte Version wird nicht mehr unterstützt. " +
                    "Bitte lade die neuste version herunter und installiere diese.",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            val uriHandler = LocalUriHandler.current
            Button(
                onClick = {
                    uriHandler.openUri("https://github.com/DatepollSystems/waiterrobot-desktop/releases/latest")
                }
            ) {
                Text("Zum Download")
            }
        }
    }
}
