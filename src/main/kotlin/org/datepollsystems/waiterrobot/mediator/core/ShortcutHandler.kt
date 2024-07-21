package org.datepollsystems.waiterrobot.mediator.core

import androidx.compose.ui.input.key.KeyEvent

object ShortcutManager {
    private val handlers = mutableSetOf<ShortcutHandler>()

    fun registerHandler(handler: ShortcutHandler) {
        handlers.add(handler)
    }

    fun removeHandler(handler: ShortcutHandler) {
        handlers.remove(handler)
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        for (handler in handlers) {
            if (handler.onKeyEvent(event)) return true
        }
        return false
    }
}

fun interface ShortcutHandler {
    /** Might be called multiple times for the same key when hold down */
    fun onKeyEvent(event: KeyEvent): Boolean
}
