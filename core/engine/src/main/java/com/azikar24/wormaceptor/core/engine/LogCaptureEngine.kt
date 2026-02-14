package com.azikar24.wormaceptor.core.engine

import android.util.Log
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * Engine that captures logcat output and exposes it as a StateFlow.
 *
 * Uses Runtime.exec to run "logcat -v threadtime" and parses the output.
 * Filters logs to the current process PID and maintains a circular buffer
 * of the most recent entries.
 *
 * Threadtime format example:
 * 01-21 14:30:45.123  1234  5678 D MyTag  : This is a log message
 * MM-DD HH:mm:ss.mmm  PID   TID  LEVEL TAG: MESSAGE
 */
class LogCaptureEngine(
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var captureJob: Job? = null
    private var process: Process? = null

    private val idGenerator = AtomicLong(0)
    private val currentPid = android.os.Process.myPid()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    // Internal buffer for circular storage
    private val buffer = ArrayDeque<LogEntry>(bufferSize)
    private val bufferLock = Any()

    // Threadtime format regex pattern
    // Example: 01-21 14:30:45.123  1234  5678 D MyTag  : This is a log message
    private val threadtimePattern = Regex(
        """^(\d{2}-\d{2})\s+(\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEA])\s+(.+?)\s*:\s*(.*)$""",
    )

    // Date format for parsing logcat timestamps
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    /**
     * Starts capturing logcat output.
     * If already capturing, this is a no-op.
     */
    fun start() {
        if (_isCapturing.value) return

        _isCapturing.value = true
        captureJob = scope.launch {
            captureLogcat()
        }
    }

    /**
     * Stops capturing logcat output.
     */
    fun stop() {
        _isCapturing.value = false
        captureJob?.cancel()
        captureJob = null
        process?.destroy()
        process = null
    }

    /**
     * Clears all captured logs.
     */
    fun clear() {
        synchronized(bufferLock) {
            buffer.clear()
        }
        _logs.value = emptyList()
    }

    /**
     * Returns the current PID being filtered.
     */
    fun getCurrentPid(): Int = currentPid

    private suspend fun captureLogcat() {
        try {
            // Clear logcat before starting to avoid old logs
            Runtime.getRuntime().exec("logcat -c").waitFor()

            // Start logcat with threadtime format
            val processBuilder = ProcessBuilder("logcat", "-v", "threadtime")
            processBuilder.redirectErrorStream(true)
            val logcatProcess = processBuilder.start()
            process = logcatProcess

            val reader = BufferedReader(InputStreamReader(logcatProcess.inputStream))
            var line: String?

            while (scope.isActive && _isCapturing.value) {
                line = reader.readLine()
                if (line != null) {
                    parseLine(line)?.let { entry ->
                        // Filter to current PID
                        if (entry.pid == currentPid) {
                            addEntry(entry)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Log capture stopped or error occurred", e)
        } finally {
            process?.destroy()
            process = null
        }
    }

    private fun parseLine(line: String): LogEntry? {
        val match = threadtimePattern.matchEntire(line) ?: return null

        val groups = match.groupValues
        val dateStr = groups[1]
        val timeStr = groups[2]
        val pidStr = groups[3]
        val tidStr = groups[4]
        val levelStr = groups[5]
        val tag = groups[6]
        val message = groups[7]

        val timestamp = try {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val parsed = dateFormat.parse("$dateStr $timeStr")
            if (parsed != null) {
                calendar.time = parsed
                calendar.set(Calendar.YEAR, year)
                calendar.timeInMillis
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        val level = LogLevel.fromTag(levelStr)
        val pid = pidStr.toIntOrNull() ?: 0
        val tid = tidStr.toIntOrNull() ?: 0

        return LogEntry(
            id = idGenerator.incrementAndGet(),
            timestamp = timestamp,
            level = level,
            tag = tag.trim(),
            pid = pid,
            tid = tid,
            message = message,
        )
    }

    private fun addEntry(entry: LogEntry) {
        synchronized(bufferLock) {
            if (buffer.size >= bufferSize) {
                buffer.removeFirst()
            }
            buffer.addLast(entry)
            _logs.value = buffer.toList()
        }
    }

    companion object {
        private const val TAG = "LogCaptureEngine"
        const val DEFAULT_BUFFER_SIZE = 1000
    }
}
