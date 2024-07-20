@file:Suppress("MaxLineLength")

package org.datepollsystems.waiterrobot.mediator.printer

import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
val NETWORK_ERROR_BASE64 by lazy {
    Base64.encode(getResourceAsStream("/network-error.pdf")!!.readBytes())
}

@OptIn(ExperimentalEncodingApi::class)
val PRINTER_CONNECTED_BASE64 by lazy {
    Base64.encode(getResourceAsStream("/printer-connected.pdf")!!.readBytes())
}

private fun getResourceAsStream(path: String): InputStream? {
    return object {}.javaClass.getResourceAsStream(path)
}
