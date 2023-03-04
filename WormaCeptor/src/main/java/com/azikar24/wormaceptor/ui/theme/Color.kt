/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptor.ui.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val whiteColor = Color(0xFFFFFFFF)
val brandPrimaryColor = Color(0xFF560BAD)
val brandVariantColor = Color(0xFFB5179E)


val mSearchHighlightBackgroundColor = Color(0xFFFFFD38)
val mSearchHighlightTextColor = Color(0xFFB5179E)


val statusError = Color(0xFFB71C1C)
val statusRequested = Color(0xFF9E9E9E)

val status300 = Color(0xFF135DCF)
val status400 = Color(0xFFFF9800)
val status500 = Color(0xFFF44336)

@get:Composable
val Colors.statusDefaultTxt: Color
    get() = if (isLight) Color(0xFF212121) else Color(0xFFF2F2F2)


@get:Composable
val Colors.statusDefault: Color
    get() = Color(0xFF4caf50)