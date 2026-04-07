package com.azikar24.wormaceptor.feature.webviewmonitor.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Web
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType

internal fun getResourceTypeIcon(type: WebViewResourceType): ImageVector {
    return when (type) {
        WebViewResourceType.DOCUMENT -> Icons.Default.Description
        WebViewResourceType.SCRIPT -> Icons.Default.Code
        WebViewResourceType.STYLESHEET -> Icons.Default.Style
        WebViewResourceType.IMAGE -> Icons.Default.Image
        WebViewResourceType.FONT -> Icons.Default.FontDownload
        WebViewResourceType.XHR -> Icons.Default.Sync
        WebViewResourceType.MEDIA -> Icons.Default.Movie
        WebViewResourceType.WEBSOCKET -> Icons.Default.Sync
        WebViewResourceType.MANIFEST -> Icons.Default.Description
        WebViewResourceType.OBJECT -> Icons.Default.Web
        WebViewResourceType.IFRAME -> Icons.Default.Web
        WebViewResourceType.OTHER -> Icons.Default.MoreHoriz
        WebViewResourceType.UNKNOWN -> Icons.Default.MoreHoriz
    }
}

internal fun getStatusColor(request: WebViewRequest): Color {
    return when {
        request.isPending -> WormaCeptorTokens.Colors.Status.amber
        request.isSuccess -> WormaCeptorTokens.Colors.Status.green
        else -> WormaCeptorTokens.Colors.Status.red
    }
}
