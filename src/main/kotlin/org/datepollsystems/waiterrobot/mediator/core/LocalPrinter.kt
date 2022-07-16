package org.datepollsystems.waiterrobot.mediator.core

import io.ktor.utils.io.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import org.datepollsystems.waiterrobot.mediator.utils.sha256
import java.util.*
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.SimpleDoc
import javax.print.event.PrintJobEvent
import javax.print.event.PrintJobListener

interface LocalPrinterInfo {
    val localId: String
    val name: String
}

class LocalPrinter(private val service: PrintService) : LocalPrinterInfo {
    override val localId = service.name.sha256() // Name should be unique (on a system level)
    override val name get() = service.name

    suspend fun print(base64data: String) {
        val coroutineJob: CompletableJob = Job()

        val decoded = Base64.getDecoder().decode(base64data)
        val doc = SimpleDoc(decoded, DocFlavor.BYTE_ARRAY.PDF, null) // TODO pass a DocName or other attributes??

        val printJob = service.createPrintJob()
        val listener = PrintJobListenerImpl(coroutineJob)
        printJob.addPrintJobListener(listener)

        printJob.print(doc, null) // TODO pass some attributes (PageSize?)

        try {
            coroutineJob.join()
        } catch (e: CancellationException) {
            if (e.cause is PrintException) {
                throw e.cause!!
            }
            throw e
        } finally {
            printJob.removePrintJobListener(listener)
        }
    }

    fun checkOnline(): Boolean {
        TODO("Implement ping or any check if the printer is still available")
    }

    inner class PrintJobListenerImpl(private val job: CompletableJob) : PrintJobListener {
        override fun printDataTransferCompleted(pje: PrintJobEvent?) {
            // TODO log
        }

        override fun printJobCompleted(pje: PrintJobEvent?) {
            job.complete()
        }

        override fun printJobFailed(pje: PrintJobEvent?) {
            job.completeExceptionally(PrintJobFailedException(pje))
        }

        override fun printJobCanceled(pje: PrintJobEvent?) {
            job.completeExceptionally(PrintJobCanceledException(pje))
        }

        override fun printJobNoMoreEvents(pje: PrintJobEvent?) {
            job.complete() // Printer does "not allow" to verify the "success", so assume it was successful
        }

        override fun printJobRequiresAttention(pje: PrintJobEvent?) {
            job.completeExceptionally(PrintJobRequiresAttentionException(pje))
        }
    }
}