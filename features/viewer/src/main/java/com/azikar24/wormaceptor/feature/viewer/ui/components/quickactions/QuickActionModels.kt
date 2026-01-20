/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.quickactions

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a quick action that can be performed on a transaction.
 */
data class QuickAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Quick filter options for filtering transaction lists.
 */
enum class QuickFilter(
    val label: String,
    val description: String,
) {
    ERRORS("Errors", "4xx and 5xx responses"),
    SLOW("Slow", "Requests taking more than 1 second"),
    LARGE("Large", "Responses larger than 100KB"),
    TODAY("Today", "Requests from today"),
    JSON("JSON", "JSON content type responses"),
    IMAGES("Images", "Image content type responses"),
}
