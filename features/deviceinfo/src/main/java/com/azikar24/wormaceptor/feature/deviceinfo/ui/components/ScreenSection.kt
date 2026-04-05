package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.ScreenDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R
import java.util.Locale

@Composable
internal fun ScreenSection(
    screen: ScreenDetails,
    onCopy: () -> Unit,
) {
    WormaCeptorInfoCard(
        title = stringResource(R.string.deviceinfo_section_display),
        icon = Icons.Default.ScreenRotation,
        iconTint = WormaCeptorTokens.Colors.Status.blue,
        onAction = onCopy,
        actionContentDescription = stringResource(
            R.string.deviceinfo_copy_section,
            stringResource(R.string.deviceinfo_section_display),
        ),
    ) {
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_resolution),
            "${screen.widthPixels} x ${screen.heightPixels}",
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_screen_density_dpi), screen.densityDpi.toString())
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_density),
            String.format(Locale.US, "%.2f", screen.density),
        )
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_scaled_density),
            String.format(Locale.US, "%.2f", screen.scaledDensity),
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_screen_size_category), screen.sizeCategory)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_screen_orientation), screen.orientation)
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_refresh_rate),
            "${screen.refreshRate.toInt()} Hz",
        )
    }
}
