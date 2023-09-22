package org.datepollsystems.waiterrobot.mediator.core.api

abstract class AuthorizedApi(
    baseUrlLoader: () -> String,
    client: AuthorizedClient
) : AbstractApi(baseUrlLoader, client.delegate)
