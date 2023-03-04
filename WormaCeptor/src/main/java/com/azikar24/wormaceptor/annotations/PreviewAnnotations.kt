/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.annotations

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "light",
    showSystemUi = true,
    device = Devices.PIXEL,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    group = "light"
)
@Preview(
    name = "dark",
    showSystemUi = true,
    device = Devices.PIXEL,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    group = "dark"

)
annotation class ScreenPreviews



@Preview(
    name = "light",
    device = Devices.PIXEL,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    group = "light"
)
@Preview(
    name = "dark",
    device = Devices.PIXEL,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    group = "dark"

)
annotation class ComponentPreviews