package org.datepollsystems.waiterrobot.mediator.data.api

import io.ktor.client.*
import io.ktor.client.statement.*
import org.datepollsystems.waiterrobot.mediator.app.AppVersion
import org.datepollsystems.waiterrobot.mediator.core.api.AbstractApi

class GitHubApi(
    client: HttpClient
) : AbstractApi("https://github.com/DatepollSystems/waiterrobot-desktop", client) {
    suspend fun getLatestVersion(): AppVersion? {
        // "releases/latest" always redirects to the latest release (e.g. "/releases/tag/v1.0.2")
        val versionString = head("releases/latest").request.url.pathSegments.last().removePrefix("v")
        return AppVersion.fromVersionStringOrNull(versionString)
    }
}