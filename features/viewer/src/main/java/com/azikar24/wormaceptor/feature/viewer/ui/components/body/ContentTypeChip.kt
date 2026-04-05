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
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.contracts.ContentType

/**
 * A chip that displays the detected content type with appropriate styling.
 * Uses semantic colors for each content type for quick visual identification.
 */
@Composable
fun ContentTypeChip(
    contentType: ContentType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val chipInfo = getContentTypeChipInfo(contentType)

    val backgroundColor by animateColorAsState(
        targetValue = chipInfo.color.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
        animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
        label = "chip_bg_color",
    )

    val borderColor by animateColorAsState(
        targetValue = chipInfo.color.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
        label = "chip_border_color",
    )

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.chip,
        color = backgroundColor,
        border = BorderStroke(
            width = WormaCeptorTokens.BorderWidth.thin,
            color = borderColor,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
        ) {
            Icon(
                imageVector = chipInfo.icon,
                contentDescription = chipInfo.displayName,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                tint = chipInfo.color,
            )
            Text(
                text = chipInfo.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = chipInfo.color,
            )
        }
    }
}

/**
 * Information about a content type for display purposes.
 *
 * @property displayName Human-readable label for the content type (e.g. "JSON", "XML").
 * @property icon Material icon representing the content type.
 * @property color Semantic color used for the chip background and text tint.
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
            color = WormaCeptorTokens.Colors.ContentType.json,
        )
        ContentType.XML -> ContentTypeChipInfo(
            displayName = "XML",
            icon = Icons.Default.Code,
            color = WormaCeptorTokens.Colors.ContentType.xml,
        )
        ContentType.HTML -> ContentTypeChipInfo(
            displayName = "HTML",
            icon = Icons.Default.Web,
            color = WormaCeptorTokens.Colors.ContentType.html,
        )
        ContentType.PROTOBUF -> ContentTypeChipInfo(
            displayName = "Protobuf",
            icon = Icons.Default.DataArray,
            color = WormaCeptorTokens.Colors.ContentType.protobuf,
        )
        ContentType.FORM_DATA -> ContentTypeChipInfo(
            displayName = "Form Data",
            icon = Icons.AutoMirrored.Filled.ViewList,
            color = WormaCeptorTokens.Colors.ContentType.formData,
        )
        ContentType.MULTIPART -> ContentTypeChipInfo(
            displayName = "Multipart",
            icon = Icons.Default.Description,
            color = WormaCeptorTokens.Colors.ContentType.multipart,
        )
        ContentType.PLAIN_TEXT -> ContentTypeChipInfo(
            displayName = "Plain Text",
            icon = Icons.Default.TextFields,
            color = WormaCeptorTokens.Colors.ContentType.plainText,
        )
        ContentType.BINARY -> ContentTypeChipInfo(
            displayName = "Binary",
            icon = Icons.Default.DataArray,
            color = WormaCeptorTokens.Colors.ContentType.binary,
        )
        ContentType.PDF -> ContentTypeChipInfo(
            displayName = "PDF",
            icon = Icons.Default.PictureAsPdf,
            color = WormaCeptorTokens.Colors.ContentType.pdf,
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
            color = WormaCeptorTokens.Colors.ContentType.image,
        )
        ContentType.UNKNOWN -> ContentTypeChipInfo(
            displayName = "Unknown",
            icon = Icons.Default.QuestionMark,
            color = WormaCeptorTokens.Colors.ContentType.unknown,
        )
    }
}
