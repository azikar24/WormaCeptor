package com.azikar24.wormaceptor.ui.drawables.myiconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.example.wormaceptor.ui.drawables.MyIconPack

val MyIconPack.IcBack: ImageVector
    get() {
        if (_IcBack != null) {
            return _IcBack!!
        }
        _IcBack = Builder(
            name = "IcBack", defaultWidth = 10.0.dp, defaultHeight = 19.0.dp,
            viewportWidth = 10.0f, viewportHeight = 19.0f
        ).apply {
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFffffff)),
                strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = EvenOdd
            ) {
                moveTo(10.0f, 18.478f)
                lineTo(0.0f, 9.624f)
                lineTo(10.0f, 0.771f)
            }
        }
            .build()
        return _IcBack!!
    }

private var _IcBack: ImageVector? = null
