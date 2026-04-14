package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.components.detail.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatDateFull
import com.azikar24.wormaceptor.domain.entities.AppDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R

@Composable
internal fun AppSection(
    app: AppDetails,
    onCopy: () -> Unit,
) {
    val sectionTitle = stringResource(R.string.deviceinfo_section_application)
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.Apps,
        iconTint = WormaCeptorTokens.Colors.Accent.tertiary,
        onAction = onCopy,
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_package_name), app.packageName)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_version_name), app.versionName)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_version_code), app.versionCode.toString())
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_target_sdk), app.targetSdk.toString())
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_min_sdk), app.minSdk.toString())
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_app_first_install),
            formatDateFull(app.firstInstallTime),
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_last_update), formatDateFull(app.lastUpdateTime))
        val debuggableValue = if (app.isDebuggable) {
            stringResource(
                R.string.deviceinfo_yes,
            )
        } else {
            stringResource(R.string.deviceinfo_no)
        }
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_debuggable), debuggableValue)
    }
}
