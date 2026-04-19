package com.azikar24.wormaceptor.feature.viewer.ui.util

sealed class ImageOperationResult(val message: String) {
    class Success(message: String) : ImageOperationResult(message)
    class Failure(message: String) : ImageOperationResult(message)
}
