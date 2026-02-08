package com.azikar24.wormaceptor.feature.viewer.ui

/**
 * Shared utilities for crash data processing.
 */
object CrashUtils {
    /**
     * Extracts the primary crash location (File:Line) from a stacktrace.
     * Looks for the first line that matches the pattern of a source file reference.
     */
    fun extractCrashLocation(stackTrace: String): String? {
        // Basic regex to find the first line in the stacktrace that looks like a source file reference
        // Matches: (FileName.kt:123) or (FileName.java:123)
        val regex = """\(([^:]+\.(?:kt|java)):(\d+)\)""".toRegex()
        val match = regex.find(stackTrace)
        return match?.let {
            val fileName = it.groupValues[1]
            val lineNumber = it.groupValues[2]
            "$fileName:$lineNumber"
        }
    }

    /**
     * Represents a single frame in a stack trace.
     */
    data class StackFrame(
        val fullLine: String,
        val packageName: String?,
        val className: String?,
        val methodName: String?,
        val fileName: String?,
        val lineNumber: Int?,
        val isAppCode: Boolean,
    )

    /**
     * Parses a stack trace string into individual frames.
     */
    fun parseStackTrace(stackTrace: String, appPackage: String = "com.azikar24.wormaceptor"): List<StackFrame> {
        return stackTrace.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                parseStackFrame(line, appPackage)
            }
    }

    /**
     * Parses a single stack frame line.
     * Example formats:
     * - at com.example.MyClass.method(MyClass.kt:123)
     * - at com.example.MyClass$inner.method(MyClass.kt:123)
     * - at java.lang.Thread.run(Thread.java:764)
     */
    private fun parseStackFrame(line: String, appPackage: String): StackFrame {
        val trimmed = line.trim()

        // Match pattern: at package.Class.method(File.kt:line)
        val atRegex = """^\s*at\s+([^\(]+)\(([^:]+):(\d+)\)""".toRegex()
        val match = atRegex.find(trimmed)

        if (match != null) {
            val fullQualifiedMethod = match.groupValues[1]
            val fileName = match.groupValues[2]
            val lineNumber = match.groupValues[3].toIntOrNull()

            // Parse package, class, and method
            val parts = fullQualifiedMethod.split(".")
            val methodName = parts.lastOrNull()
            val className = parts.dropLast(1).lastOrNull()
            val packageName = parts.dropLast(2).joinToString(".")

            val isAppCode = fullQualifiedMethod.startsWith(appPackage)

            return StackFrame(
                fullLine = trimmed,
                packageName = packageName,
                className = className,
                methodName = methodName,
                fileName = fileName,
                lineNumber = lineNumber,
                isAppCode = isAppCode,
            )
        }

        // If no match, still include the line for completeness
        return StackFrame(
            fullLine = trimmed,
            packageName = null,
            className = null,
            methodName = null,
            fileName = null,
            lineNumber = null,
            isAppCode = false,
        )
    }
}
