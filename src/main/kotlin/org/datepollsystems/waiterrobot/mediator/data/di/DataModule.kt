package org.datepollsystems.waiterrobot.mediator.data.di

import org.datepollsystems.waiterrobot.mediator.data.api.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::AuthApi)
    singleOf(::EventApi)
    singleOf(::GitHubApi)
    singleOf(::OrganisationApi)
    singleOf(::PrinterApi)
}