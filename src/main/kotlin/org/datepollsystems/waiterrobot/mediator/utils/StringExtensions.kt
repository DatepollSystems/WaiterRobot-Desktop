package org.datepollsystems.waiterrobot.mediator.utils

fun String?.emptyToNull(): String? = this?.ifEmpty { null }

fun String.sha256() = this.toByteArray().sha256()
