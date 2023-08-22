package org.datepollsystems.waiterrobot.mediator.core.api

abstract class AuthorizedApi(basePath: String, client: AuthorizedClient) : AbstractApi(basePath, client.delegate)
