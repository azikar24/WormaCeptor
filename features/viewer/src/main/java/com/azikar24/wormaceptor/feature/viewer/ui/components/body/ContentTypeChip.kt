package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.TextFields
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
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.contracts.ContentType

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
            color = WormaCeptorColors.ContentType.Json,
        )
        ContentType.XML -> ContentTypeChipInfo(
            displayName = "XML",
            icon = Icons.Default.Code,
            color = WormaCeptorColors.ContentType.Xml,
        )
        ContentType.HTML -> ContentTypeChipInfo(
            displayName = "HTML",
            icon = Icons.Default.Web,
            color = WormaCeptorColors.ContentType.Html,
        )
        ContentType.PROTOBUF -> ContentTypeChipInfo(
            displayName = "Protobuf",
            icon = Icons.Default.DataArray,
            color = WormaCeptorColors.ContentType.Protobuf,
        )
        ContentType.FORM_DATA -> ContentTypeChipInfo(
            displayName = "Form Data",
            icon = Icons.AutoMirrored.Filled.ViewList,
            color = WormaCeptorColors.ContentType.FormData,
        )
        ContentType.MULTIPART -> ContentTypeChipInfo(
            displayName = "Multipart",
            icon = Icons.Default.Description,
            color = WormaCeptorColors.ContentType.Multipart,
        )
        ContentType.PLAIN_TEXT -> ContentTypeChipInfo(
            displayName = "Plain Text",
            icon = Icons.Default.TextFields,
            color = WormaCeptorColors.ContentType.PlainText,
        )
        ContentType.BINARY -> ContentTypeChipInfo(
            displayName = "Binary",
            icon = Icons.Default.DataArray,
            color = WormaCeptorColors.ContentType.Binary,
        )
        ContentType.PDF -> ContentTypeChipInfo(
            displayName = "PDF",
            icon = Icons.Default.PictureAsPdf,
            color = WormaCeptorColors.ContentType.Pdf,
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
            color = WormaCeptorColors.ContentType.Image,
        )
        ContentType.UNKNOWN -> ContentTypeChipInfo(
            displayName = "Unknown",
            icon = Icons.Default.QuestionMark,
            color = WormaCeptorColors.ContentType.Unknown,
        )
    }
}
