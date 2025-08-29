package org.datepollsystems.waiterrobot.mediator.core.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.BuildType
import org.datepollsystems.waiterrobot.mediator.core.api.createAuthorizedClient
import org.datepollsystems.waiterrobot.mediator.core.api.createClient
import org.datepollsystems.waiterrobot.mediator.core.rollingFileLogWriter
import org.datepollsystems.waiterrobot.mediator.core.sentry.SentryLogWriter
import org.koin.dsl.module

val coreModule = module {
    val baseLogger = Logger(
        StaticConfig(
            when (App.buildType) {
                BuildType.DEBUG -> Severity.Verbose
                BuildType.RELEASE -> Severity.Info
            },
            logWriterList = buildList {
                add(platformLogWriter())
                add(SentryLogWriter())
                if (App.buildType == BuildType.RELEASE) add(rollingFileLogWriter())
            },
        ),
        tag = "WaiterRobot"
    )
    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag) else baseLogger }

    single { createClient(logger = getLogger("Basic HttpClient")) }
    single { createAuthorizedClient(logger = getLogger("Authorized HttpClient")) }
}
