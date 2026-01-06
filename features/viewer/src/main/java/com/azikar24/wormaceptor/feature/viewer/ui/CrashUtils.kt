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
}
