package com.azikar24.wormaceptor.feature.database.vm

sealed class DatabaseViewEffect {
    data class ShowError(val message: String) : DatabaseViewEffect()
}
