package org.datepollsystems.waiterrobot.mediator.printer

import org.apache.pdfbox.pdmodel.PDDocument
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.di.injectLoggerForClass
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object VirtualLocalPrinter : AbstractLocalPrinter() {
    override val localId: String = "virtualPrinter"
    override val name: String = "Virtual Printer"
    private val basePath: String = Path(App.config.basePath, "virtualPrinters").absolutePathString()
    private val logger by injectLoggerForClass()

    init {
        logger.i("Will save to: $basePath")
    }

    override fun printPdf(pdfId: String, bePrinterId: ID, document: PDDocument) {
        val file = Path(basePath, bePrinterId.toString(), "$pdfId.pdf").toFile()
        file.parentFile.mkdirs()
        document.save(file)
    }
}
