package org.datepollsystems.waiterrobot.mediator.data.api

import io.ktor.client.call.*
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.api.AuthorizedApi
import org.datepollsystems.waiterrobot.mediator.core.api.AuthorizedClient
import org.datepollsystems.waiterrobot.mediator.data.api.dto.GetOrganisationDto

class OrganisationApi(
    client: AuthorizedClient
) : AuthorizedApi({ "${App.config.apiBase}config/organisation" }, client) {
    suspend fun getUserOrganisations() = get("/").body<List<GetOrganisationDto>>()
}
