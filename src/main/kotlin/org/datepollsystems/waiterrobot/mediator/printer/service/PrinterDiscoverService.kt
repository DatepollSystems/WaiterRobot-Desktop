package org.datepollsystems.waiterrobot.mediator.printer.service

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
            .associateBy { it.localId }
    }

    fun addVirtualPrinter() {
        if (_localPrinterMap.contains(VirtualLocalPrinter.localId)) return
        synchronized(_localPrinterMap) {
            _localPrinterMap = _localPrinterMap.plus(VirtualLocalPrinter.localId to VirtualLocalPrinter)
        }
    }

    val localPrinters: Collection<LocalPrinterInfo> get() = localPrinterMap.values
}
