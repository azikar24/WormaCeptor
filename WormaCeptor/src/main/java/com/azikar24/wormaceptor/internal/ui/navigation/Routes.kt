/*
 * Copyright AziKar24 22/12/2025.
 */

package com.azikar24.wormaceptor.internal.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object NetworkList : Route

    @Serializable
    data object CrashesList : Route

    @Serializable
    data class NetworkDetails(val id: Long) : Route

    @Serializable
    data class CrashDetails(val id: Long) : Route
}