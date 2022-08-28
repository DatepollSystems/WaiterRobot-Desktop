package org.datepollsystems.waiterrobot.mediator.utils

import java.security.MessageDigest

fun ByteArray.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    val hex = md.digest(this)
    return hex.toHex()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHex(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }