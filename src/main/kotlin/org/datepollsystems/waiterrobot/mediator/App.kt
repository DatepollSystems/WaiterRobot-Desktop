package org.datepollsystems.waiterrobot.mediator

import org.datepollsystems.waiterrobot.mediator.ui.startUI
import org.datepollsystems.waiterrobot.mediator.ws.WsClient

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        startUI(onClose = { WsClient.stop() })
    }
}