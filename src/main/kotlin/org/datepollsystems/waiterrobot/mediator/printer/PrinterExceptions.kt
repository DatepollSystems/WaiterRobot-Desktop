package org.datepollsystems.waiterrobot.mediator.printer

import javax.print.event.PrintJobEvent

open class PrintException(val pje: PrintJobEvent?) : Exception()
class PrintJobFailedException(pje: PrintJobEvent?) : PrintException(pje)
class PrintJobCanceledException(pje: PrintJobEvent?) : PrintException(pje)
class PrintJobRequiresAttentionException(pje: PrintJobEvent?) : PrintException(pje)

class PrinterWithIdNotFoundException(val id: Long) : IllegalArgumentException("Could not find printer with id: $id")