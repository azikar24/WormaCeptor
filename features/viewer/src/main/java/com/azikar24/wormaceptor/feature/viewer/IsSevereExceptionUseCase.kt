package com.azikar24.wormaceptor.feature.viewer

internal class IsSevereExceptionUseCase {
    operator fun invoke(exceptionType: String): Boolean =
        SEVERE_TYPES.any { exceptionType.contains(it, ignoreCase = true) }

    companion object {
        private val SEVERE_TYPES = listOf(
            "NullPointerException",
            "OutOfMemoryError",
            "StackOverflowError",
            "SecurityException",
            "IllegalStateException",
            "AssertionError",
        )
    }
}
