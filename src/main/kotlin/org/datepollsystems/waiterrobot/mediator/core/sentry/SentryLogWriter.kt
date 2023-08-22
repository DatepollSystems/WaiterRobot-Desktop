package org.datepollsystems.waiterrobot.mediator.core.sentry

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel

class SentryLogWriter : LogWriter() {
    override fun isLoggable(tag: String, severity: Severity): Boolean = when (severity) {
        Severity.Verbose, Severity.Debug -> false
        Severity.Info, Severity.Warn, Severity.Error, Severity.Assert -> true
    }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        when (severity) {
            Severity.Verbose, Severity.Debug -> return // Never log Verbose and Debug (see isLoggable)
            Severity.Info -> {
                Sentry.addBreadcrumb(Breadcrumb.info(message))
            }

            Severity.Warn, Severity.Error, Severity.Assert -> {
                if (throwable != null) {
                    Sentry.captureException(throwable) { scope ->
                        scope.level = severity.toSentryLevel()
                        scope.setExtra("message", message)
                        scope.setExtra("tag", tag)
                    }
                } else {
                    Sentry.captureMessage(message) { scope ->
                        scope.level = severity.toSentryLevel()
                        scope.setExtra("tag", message)
                    }
                }
            }
        }
    }

    private fun Severity.toSentryLevel(): SentryLevel = when (this) {
        Severity.Verbose, Severity.Debug -> SentryLevel.DEBUG
        Severity.Info -> SentryLevel.INFO
        Severity.Warn -> SentryLevel.WARNING
        Severity.Error -> SentryLevel.ERROR
        Severity.Assert -> SentryLevel.FATAL
    }
}
