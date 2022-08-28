package org.datepollsystems.waiterrobot.mediator

import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.ui.startUI
import org.datepollsystems.waiterrobot.mediator.utils.isLazyInitialized
import org.datepollsystems.waiterrobot.mediator.ws.MediatorWebSocketManager


object App {
    private val logoutListeners: MutableList<() -> Unit> = mutableListOf()

    val socketManager: MediatorWebSocketManager by lazy {
        MediatorWebSocketManager()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        startUI(this::onClose)
    }

    private fun onClose() {
        if (App::socketManager.isLazyInitialized) {
            socketManager.close()
        }
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