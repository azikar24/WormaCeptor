/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem

/**
 * A chip that displays the detected content type with appropriate styling.
 * Uses semantic colors for each content type for quick visual identification.
 */
@Composable
fun ContentTypeChip(
    contentType: ContentType,
    modifier: Modifier = Modifier,
    isAutoDetected: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val chipInfo = getContentTypeChipInfo(contentType)

    val backgroundColor by animateColorAsState(
        targetValue = chipInfo.color.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        label = "chip_bg_color",
    )

    val borderColor by animateColorAsState(
        targetValue = chipInfo.color.copy(alpha = 0.3f),
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        label = "chip_border_color",
    )

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = backgroundColor,
        border = BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.thin,
            color = borderColor,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            Icon(
                imageVector = chipInfo.icon,
                contentDescription = chipInfo.displayName,
                modifier = Modifier.size(14.dp),
                tint = chipInfo.color,
            )
            Text(
                text = chipInfo.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = chipInfo.color,
            )
            if (isAutoDetected) {
                Text(
                    text = "(auto)",
                    style = MaterialTheme.typography.labelSmall,
                    color = chipInfo.color.copy(alpha = 0.7f),
                )
            }
        }
    }
}

/**
 * Information about a content type for display purposes.
 */
data class ContentTypeChipInfo(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
)

/**
 * Returns display information for each content type.
 * Colors are designed to be visually distinct and semantically meaningful.
 */
@Composable
fun getContentTypeChipInfo(contentType: ContentType): ContentTypeChipInfo {
    return when (contentType) {
        ContentType.JSON -> ContentTypeChipInfo(
            displayName = "JSON",
            icon = Icons.Default.DataObject,
            color = Color(0xFFF59E0B), // Amber
        )
        ContentType.XML -> ContentTypeChipInfo(
            displayName = "XML",
            icon = Icons.Default.Code,
            color = Color(0xFF8B5CF6), // Purple
        )
        ContentType.HTML -> ContentTypeChipInfo(
            displayName = "HTML",
            icon = Icons.Default.Web,
            color = Color(0xFFEC4899), // Pink
        )
        ContentType.PROTOBUF -> ContentTypeChipInfo(
            displayName = "Protobuf",
            icon = Icons.Default.DataArray,
            color = Color(0xFF10B981), // Emerald
        )
        ContentType.FORM_DATA -> ContentTypeChipInfo(
            displayName = "Form Data",
            icon = Icons.Default.ViewList,
            color = Color(0xFF3B82F6), // Blue
        )
        ContentType.MULTIPART -> ContentTypeChipInfo(
            displayName = "Multipart",
            icon = Icons.Default.Description,
            color = Color(0xFF6366F1), // Indigo
        )
        ContentType.PLAIN_TEXT -> ContentTypeChipInfo(
            displayName = "Plain Text",
            icon = Icons.Default.TextFields,
            color = Color(0xFF6B7280), // Gray
        )
        ContentType.BINARY -> ContentTypeChipInfo(
            displayName = "Binary",
            icon = Icons.Default.DataArray,
            color = Color(0xFFEF4444), // Red
        )
        ContentType.PDF -> ContentTypeChipInfo(
            displayName = "PDF",
            icon = Icons.Default.PictureAsPdf,
            color = Color(0xFFDC2626), // Red-600
        )
        ContentType.IMAGE_PNG,
        ContentType.IMAGE_JPEG,
        ContentType.IMAGE_GIF,
        ContentType.IMAGE_WEBP,
        ContentType.IMAGE_SVG,
        ContentType.IMAGE_BMP,
        ContentType.IMAGE_ICO,
        ContentType.IMAGE_OTHER,
        -> ContentTypeChipInfo(
            displayName = "Image",
            icon = Icons.Default.Image,
            color = Color(0xFF14B8A6), // Teal
        )
        ContentType.UNKNOWN -> ContentTypeChipInfo(
            displayName = "Unknown",
            icon = Icons.Default.QuestionMark,
            color = Color(0xFF9CA3AF), // Gray-400
        )
    }
}
