/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.home.bottomnav

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.ui.features.destinations.CrashesListScreenDestination
import com.azikar24.wormaceptor.internal.ui.features.destinations.DirectionDestination
import com.azikar24.wormaceptor.internal.ui.features.destinations.NetworkListScreenDestination
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcNetworking
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcStacktrace
import com.example.wormaceptor.ui.drawables.MyIconPack

enum class BottomBarDestination(
    val direction: DirectionDestination,
    val icon: ImageVector,
    @StringRes val label: Int,
) {
    Network(NetworkListScreenDestination, MyIconPack.IcNetworking, R.string.network),
    Crashes(CrashesListScreenDestination, MyIconPack.IcStacktrace, R.string.crashes),
}