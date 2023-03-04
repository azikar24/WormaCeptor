/*
 * Copyright AziKar24 1/3/2023.
 */

package com.azikar24.wormaceptor.ui.drawables.myiconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.example.wormaceptor.ui.drawables.MyIconPack

val MyIconPack.IcArrowDown: ImageVector
    get() {
        if (_IcArrowDown != null) {
            return _IcArrowDown!!
        }
        _IcArrowDown = Builder(name = "IcArrowDownWhite24dp", defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(7.41f, 7.84f)
                lineTo(12.0f, 12.42f)
                lineToRelative(4.59f, -4.58f)
                lineTo(18.0f, 9.25f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(-6.0f, -6.0f)
                close()
            }
        }
            .build()
        return _IcArrowDown!!
    }

private var _IcArrowDown: ImageVector? = null
