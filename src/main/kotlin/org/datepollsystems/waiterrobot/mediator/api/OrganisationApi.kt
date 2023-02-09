package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.call.*
import org.datepollsystems.waiterrobot.mediator.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.app.Config

class OrganisationApi(override val client: HttpClient) : AbstractApi("${Config.API_BASE}config/organisation") {
    suspend fun getUserOrganisations() = get("/").body<List<GetOrganisationDto>>()
}