/*
 * Copyright AziKar24 23/12/2025.
 */

package com.azikar24.wormaceptor.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

class ToolbarViewModel : ViewModel() {
    var title by mutableStateOf("")
    var subtitle by mutableStateOf<String?>(null)
    var showSearch by mutableStateOf(false)
    var searchKey by mutableStateOf("")
    var color by mutableStateOf<Color?>(null)
    var onColor by mutableStateOf<Color?>(null)
    var menuActions by mutableStateOf<(@Composable () -> Unit)?>(null)

    fun reset() {
        title = ""
        subtitle = null
        showSearch = false
        searchKey = ""
        color = null
        onColor = null
        menuActions = null
    }
}
