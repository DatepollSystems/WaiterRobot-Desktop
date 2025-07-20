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
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.Res
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.demo_event_info
import org.jetbrains.compose.resources.stringResource

@Composable
fun DemoEventInfo(isDemoEvent: Boolean?) {
    if (isDemoEvent != true) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Red.copy(alpha = 0.9f),
        contentColor = Color.Black
    ) {
        Text(
            text = stringResource(Res.string.demo_event_info),
            modifier = Modifier.padding(5.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
