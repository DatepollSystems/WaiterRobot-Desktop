package org.datepollsystems.waiterrobot.mediator.data.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.api.AuthorizedApi
import org.datepollsystems.waiterrobot.mediator.core.api.AuthorizedClient
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetPrinterDto

class PrinterApi(client: AuthorizedClient) : AuthorizedApi("${App.config.apiBase}config/printer", client) {
    suspend fun getEventPrinters(eventId: ID) = get("/") {
        parameter("eventId", eventId)
    }.body<List<GetPrinterDto>>()
}
