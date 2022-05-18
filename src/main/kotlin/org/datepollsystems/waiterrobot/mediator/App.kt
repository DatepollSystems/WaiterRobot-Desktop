package org.datepollsystems.waiterrobot.mediator

import org.datepollsystems.waiterrobot.mediator.ui.startUI


object App {
    private val logoutListeners: MutableList<() -> Unit> = mutableListOf()

    @JvmStatic
    fun main(args: Array<String>) {
        startUI()
    }

    fun addLogoutListener(listener: () -> Unit) {
        logoutListeners.add(listener)
    }

    fun removeLogoutListener(listener: () -> Unit) {
        logoutListeners.remove(listener)
    }

    fun logout() {
        System.clearProperty("accessToken")
        System.clearProperty("sessionToken")
        logoutListeners.forEach { it.invoke() }
    }
}