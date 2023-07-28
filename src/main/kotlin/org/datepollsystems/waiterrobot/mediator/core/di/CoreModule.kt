package org.datepollsystems.waiterrobot.mediator.core.di

import org.datepollsystems.waiterrobot.mediator.core.api.createAuthorizedClient
import org.datepollsystems.waiterrobot.mediator.core.api.createClient
import org.koin.dsl.module

val coreModule = module {
    single { createClient() }
    single { createAuthorizedClient() }
}