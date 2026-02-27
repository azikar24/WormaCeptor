package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.entities.Crash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/** Captures uncaught exceptions and persists them as crash reports. */
class CrashReporter(
    private val repository: CrashRepository,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Installs the uncaught exception handler to capture crashes. */
    fun init() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            handleCrash(paramThrowable)

            // Delegate to original handler or kill
            if (oldHandler != null) {
                oldHandler.uncaughtException(paramThread, paramThrowable)
            } else {
                exitProcess(2)
            }
        }
    }

    private fun handleCrash(throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTraceString = sw.toString()

            val crash = Crash(
                timestamp = System.currentTimeMillis(),
                exceptionType = throwable.javaClass.simpleName,
                message = throwable.message,
                stackTrace = stackTraceString,
            )

            // Blocking save attempt before app dies (best effort)
            scope.launch {
                repository.saveCrash(crash)
            }

            // Sleep briefly to allow DB write? (Hack but common)
            Thread.sleep(500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
