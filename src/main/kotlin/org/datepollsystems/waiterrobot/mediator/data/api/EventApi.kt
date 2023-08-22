package org.datepollsystems.waiterrobot.mediator.data.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.api.AuthorizedApi
import org.datepollsystems.waiterrobot.mediator.core.api.AuthorizedClient
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetEventDto

class EventApi(client: AuthorizedClient) : AuthorizedApi("${App.config.apiBase}config/event", client) {
    suspend fun getOrganisationEvents(organisationId: ID) = get("/") {
        parameter("organisationId", organisationId)
    }.body<List<GetEventDto>>()
}
