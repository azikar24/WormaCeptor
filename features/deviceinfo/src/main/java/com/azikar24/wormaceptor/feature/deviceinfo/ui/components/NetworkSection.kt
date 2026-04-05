package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.NetworkDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R

@Composable
internal fun NetworkSection(
    network: NetworkDetails,
    onCopy: () -> Unit,
) {
    val connected = stringResource(R.string.deviceinfo_connected)
    val notConnected = stringResource(R.string.deviceinfo_not_connected)
    WormaCeptorInfoCard(
        title = stringResource(R.string.deviceinfo_section_network),
        icon = Icons.Default.NetworkCheck,
        iconTint = if (network.isConnected) WormaCeptorTokens.Colors.Status.green else WormaCeptorTokens.Colors.Status.red,
        onAction = onCopy,
        actionContentDescription = stringResource(
            R.string.deviceinfo_copy_section,
            stringResource(R.string.deviceinfo_section_network),
        ),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_network_connection_type), network.connectionType)

        val connectedLabel = if (network.isConnected) {
            stringResource(R.string.deviceinfo_yes)
        } else {
            stringResource(R.string.deviceinfo_no)
        }

        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_network_connected),
            connectedLabel,
        )
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_network_wifi),
            if (network.isWifiConnected) connected else notConnected,
        )
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_network_cellular),
            if (network.isCellularConnected) connected else notConnected,
        )

        val meteredLabel = if (network.isMetered) {
            stringResource(R.string.deviceinfo_yes)
        } else {
            stringResource(R.string.deviceinfo_no)
        }

        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_network_metered), meteredLabel)

        network.cellularNetworkType?.let {
            WormaCeptorDetailRow(
                stringResource(R.string.deviceinfo_network_cellular_type),
                it,
            )
        }
    }
}
