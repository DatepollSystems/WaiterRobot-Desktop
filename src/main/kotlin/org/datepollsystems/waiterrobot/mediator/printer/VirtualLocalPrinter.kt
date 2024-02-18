package org.datepollsystems.waiterrobot.mediator.printer

import org.apache.pdfbox.pdmodel.PDDocument
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.di.injectLogger
import java.io.File
import kotlin.io.path.Path

class VirtualLocalPrinter(number: Int) : AbstractLocalPrinter() {
    override val localId: String = "virtualPrinter_$number"
    override val name: String = "Virtual Printer $number"
    private val basePath: File = Path(App.config.basePath, "virtualPrinters", number.toString()).toFile()
    private val logger by injectLogger(this::class.simpleName + "#$localId")

    init {
        basePath.mkdirs()
        logger.i("Will save to: ${basePath.absolutePath}")
    }

    override fun printPdf(pdfId: String, document: PDDocument) {
        document.save(File(basePath, "$pdfId.pdf"))
    }
}
