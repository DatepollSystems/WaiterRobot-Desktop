package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.call.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.api.dto.GetOrganisationDto

class OrganisationApi(override val client: HttpClient) : AbstractApi("${App.config.apiBase}config/organisation") {
    suspend fun getUserOrganisations() = get("/").body<List<GetOrganisationDto>>()
}