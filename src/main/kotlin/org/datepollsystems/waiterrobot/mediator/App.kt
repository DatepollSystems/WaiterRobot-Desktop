package org.datepollsystems.waiterrobot.mediator

import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.core.di.initKoin
import org.datepollsystems.waiterrobot.mediator.ui.startUI
import org.datepollsystems.waiterrobot.mediator.utils.isLazyInitialized
import org.datepollsystems.waiterrobot.mediator.ws.MediatorWebSocketManager

object App {
    var config: Config = Config.Prod // Use prod config for start and change then to the "right" at login

    val socketManager: MediatorWebSocketManager by lazy {
        MediatorWebSocketManager()
    }

    private val logoutListeners: MutableList<() -> Unit> = mutableListOf()

    @JvmStatic
    fun main(args: Array<String>) {
        initKoin()
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
        Settings.loginPrefix = null
        logoutListeners.forEach { it.invoke() }
    }
}