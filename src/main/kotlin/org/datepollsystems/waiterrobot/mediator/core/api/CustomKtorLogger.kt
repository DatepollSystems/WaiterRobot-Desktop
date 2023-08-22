package org.datepollsystems.waiterrobot.mediator.core.api

import io.ktor.client.plugins.logging.*
import org.datepollsystems.waiterrobot.mediator.core.di.injectLogger
import org.koin.core.component.KoinComponent

class CustomKtorLogger(tagSuffix: String? = null) : Logger, KoinComponent {
    private val logger by injectLogger("Ktor" + tagSuffix?.let { "-$it" }.orEmpty())

    override fun log(message: String) {
        logger.d { message }
    }
}
