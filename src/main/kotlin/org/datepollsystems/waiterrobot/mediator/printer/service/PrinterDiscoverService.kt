package org.datepollsystems.waiterrobot.mediator.printer.service

import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinter
import org.datepollsystems.waiterrobot.mediator.printer.LocalPrinterInfo
import javax.print.DocFlavor
import javax.print.PrintServiceLookup

object PrinterDiscoverService {
    // Get all printers that support printing PDF's in ByteArray format
    val localPrinterMap: Map<String, LocalPrinter> = PrintServiceLookup
        .lookupPrintServices(DocFlavor.BYTE_ARRAY.PDF, null)
        .map { LocalPrinter(it) }
        .associateBy { it.localId }

    val localPrinters: Collection<LocalPrinterInfo> get() = localPrinterMap.values
}