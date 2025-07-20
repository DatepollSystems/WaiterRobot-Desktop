package org.datepollsystems.waiterrobot.mediator.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*

fun Modifier.onEnterKeyDown(block: () -> Unit) = this.onPreviewKeyEvent {
    if (it.type == KeyEventType.KeyDown && it.key == Key.Enter) {
        block()
        true
    } else {
        false
    }
}
