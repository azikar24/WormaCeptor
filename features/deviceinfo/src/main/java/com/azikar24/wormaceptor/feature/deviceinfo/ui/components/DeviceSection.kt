package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorInfoCard
import com.azikar24.wormaceptor.domain.entities.DeviceDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R

@Composable
internal fun DeviceSection(
    device: DeviceDetails,
    onCopy: () -> Unit,
) {
    val sectionTitle = stringResource(R.string.deviceinfo_section_device)
    val yes = stringResource(R.string.deviceinfo_yes)
    val no = stringResource(R.string.deviceinfo_no)
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.PhoneAndroid,
        iconTint = MaterialTheme.colorScheme.primary,
        onAction = onCopy,
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_manufacturer), device.manufacturer)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_model), device.model)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_brand), device.brand)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_device), device.device)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_hardware), device.hardware)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_board), device.board)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_product), device.product)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_emulator), if (device.isEmulator) yes else no)
    }
}
