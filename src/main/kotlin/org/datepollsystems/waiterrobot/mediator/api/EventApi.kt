package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import org.datepollsystems.waiterrobot.mediator.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.core.ID

class EventApi(override val client: HttpClient) : AbstractApi("${Config.API_BASE}config/event") {
    suspend fun getOrganisationEvents(organisationId: ID) = get<List<GetEventDto>>("/") {
        url.encodedParameters.append("organisationId", organisationId.toString())
    }
}