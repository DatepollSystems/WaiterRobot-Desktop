package org.datepollsystems.waiterrobot.mediator.utils

import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.core.di.injectLoggerForClass
import org.koin.core.component.KoinComponent
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import kotlin.system.exitProcess

object SingleAppInstanceLock : KoinComponent {
    private val logger by injectLoggerForClass()
    private var file: File? = null
    private var channel: FileChannel? = null
    private var lock: FileLock? = null

    fun ensureSingleInstance() {
        if (tryLock()) {
            logger.i { "Aquired instance lock" }
            // This process holds the lock and is the single instance allowed to run
            Runtime.getRuntime().addShutdownHook(object : Thread() {
                // destroy the lock when the JVM is closing
                override fun run() {
                    closeLock()
                    deleteFile()
                }
            })
        } else {
            // There is already another instance of this app running -> exit this process
            logger.i { "Kellner.team is already running. Closing this instance." }
            exitProcess(1)
        }
    }

    private fun tryLock(): Boolean {
        @Suppress("TooGenericExceptionCaught")
        return try {
            file = File(App.config.basePath, "kellner.team.lock").also {
                it.parentFile.mkdirs() // On Windows the parent folder must exist before creating the lock file
            }
            channel = RandomAccessFile(file, "rw").getChannel()
            lock = channel!!.tryLock()

            if (lock == null) {
                closeLock()
                return false
            }

            true
        } catch (e: Exception) {
            logger.e(e) { "Could not aquire lock" }
            closeLock()
            false
        }
    }

    private fun closeLock() {
        try {
            lock?.release()
        } catch (_: Exception) {
        }
        try {
            channel?.close()
        } catch (_: Exception) {
        }
    }

    private fun deleteFile() {
        try {
            file?.delete()
        } catch (_: Exception) {
        }
    }
}
