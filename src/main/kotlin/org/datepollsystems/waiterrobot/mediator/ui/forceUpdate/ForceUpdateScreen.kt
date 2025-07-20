package org.datepollsystems.waiterrobot.mediator.ui.forceUpdate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.Res
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.update_go_to_download
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.update_required_description
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.update_required_title
import org.jetbrains.compose.resources.stringResource

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
                text = stringResource(Res.string.update_required_title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = stringResource(Res.string.update_required_description),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            val uriHandler = LocalUriHandler.current
            Button(
                onClick = {
                    uriHandler.openUri("https://get.kellner.team/download.html")
                }
            ) {
                Text(stringResource(Res.string.update_go_to_download))
            }
        }
    }
}
