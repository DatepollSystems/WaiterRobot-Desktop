package org.datepollsystems.waiterrobot.mediator.data.di

import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.data.api.EventApi
import org.datepollsystems.waiterrobot.mediator.data.api.OrganisationApi
import org.datepollsystems.waiterrobot.mediator.data.api.PrinterApi
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::AuthApi)
    singleOf(::EventApi)
    singleOf(::OrganisationApi)
    singleOf(::PrinterApi)
}
