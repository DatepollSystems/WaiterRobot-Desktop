package org.datepollsystems.waiterrobot.mediator.printer.service

import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.printer.AbstractLocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo
import org.datepollsystems.waiterrobot.mediator.printer.VirtualLocalPrinter
import javax.print.DocFlavor
import javax.print.PrintServiceLookup

object PrinterDiscoverService {
    // Get all printers that support printing PDF's in ByteArray format
    val localPrinterMap: Map<String, AbstractLocalPrinter> get() = _localPrinterMap
    private var _localPrinterMap: Map<String, AbstractLocalPrinter> = emptyMap()

    init {
        refreshPrinters()
    }

    fun refreshPrinters() {
        _localPrinterMap = PrintServiceLookup
            .lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
            .map { LocalPrinter(it) }
            .let {
                if (App.config !is Config.Prod) {
                    it.plus(VirtualLocalPrinter(1))
                } else {
                    it
                }
            }
            .associateBy { it.localId }
    }

    val localPrinters: Collection<LocalPrinterInfo> get() = localPrinterMap.values
}
