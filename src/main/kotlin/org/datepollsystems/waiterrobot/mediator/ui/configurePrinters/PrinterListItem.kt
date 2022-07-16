package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.mediator.ui.theme.Typography

@Composable
fun PrinterListItem(title: String, subtitle: String, selected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .background(if (selected) MaterialTheme.colors.secondary.copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)) {
            Text(text = title)
            Text(text = subtitle, style = Typography.caption, color = Color.LightGray)
        }
    }
}