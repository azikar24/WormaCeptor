package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.components.detail.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.OsDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R

@Composable
internal fun OsSection(
    os: OsDetails,
    onCopy: () -> Unit,
) {
    WormaCeptorInfoCard(
        title = stringResource(R.string.deviceinfo_section_os),
        icon = Icons.Default.SystemUpdate,
        iconTint = WormaCeptorTokens.Colors.Status.green,
        onAction = onCopy,
        actionContentDescription = stringResource(
            R.string.deviceinfo_copy_section,
            stringResource(R.string.deviceinfo_section_os),
        ),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_android_version), os.androidVersion)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_sdk_level), os.sdkLevel.toString())
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_build_id), os.buildId)
        os.securityPatch?.let { WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_security_patch), it) }
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_bootloader), os.bootloader)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_incremental), os.incremental)
        CollapsibleInfoRow(stringResource(R.string.deviceinfo_os_fingerprint), os.fingerprint)
    }
}
