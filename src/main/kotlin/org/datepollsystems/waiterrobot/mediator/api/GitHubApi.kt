package org.datepollsystems.waiterrobot.mediator.api

import io.ktor.client.*
import io.ktor.client.statement.*
import org.datepollsystems.waiterrobot.mediator.app.AppVersion

class GitHubApi(
    override val client: HttpClient
) : AbstractApi("https://github.com/DatepollSystems/waiterrobot-desktop") {
    suspend fun getLatestVersion(): AppVersion? {
        // "releases/latest" always redirects to the latest release (e.g. "/releases/tag/v1.0.2")
        val versionString = head("releases/latest").request.url.pathSegments.last().removePrefix("v")
        return AppVersion.fromVersionStringOrNull(versionString)
    }
}