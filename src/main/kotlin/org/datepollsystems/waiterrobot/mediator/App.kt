package org.datepollsystems.waiterrobot.mediator

import kotlinx.coroutines.runBlocking
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.ui.startUI
import org.datepollsystems.waiterrobot.mediator.ws.WsClient


object App {
    private val logoutListeners: MutableList<() -> Unit> = mutableListOf()

    @JvmStatic
    fun main(args: Array<String>) {
        startUI(this::onClose)
    }

    private fun onClose() {
        runBlocking { WsClient.stop() }
    }

    fun addLogoutListener(listener: () -> Unit) {
        logoutListeners.add(listener)
    }

    fun removeLogoutListener(listener: () -> Unit) {
        logoutListeners.remove(listener)
    }

    fun logout() {
        Settings.accessToken = null
        Settings.refreshToken = null
        logoutListeners.forEach { it.invoke() }
    }
}