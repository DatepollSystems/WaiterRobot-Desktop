package org.datepollsystems.waiterrobot.mediator.printer

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.koin.core.component.KoinComponent
import java.util.*

/**
 * Manages a local Printer
 *
 * for an example see [org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterTest.testPrint]
 *
 * @author Fabian Schedler
 * @see org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterTest.testPrint
 */
abstract class AbstractLocalPrinter : LocalPrinterInfo, KoinComponent {
    fun printPdf(pdfId: String, base64data: String) {
        val decoded = Base64.getDecoder().decode(base64data)
        printPdf(pdfId, Loader.loadPDF(decoded))
    }

    protected abstract fun printPdf(pdfId: String, document: PDDocument)

    override fun equals(other: Any?): Boolean {
        if (other !is AbstractLocalPrinter) return false
        return this.localId == other.localId
    }

    override fun hashCode(): Int = this.localId.hashCode()
}

interface LocalPrinterInfo {
    val localId: String
    val name: String
}
