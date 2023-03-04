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

val MyIconPack.IcNetworking: ImageVector
    get() {
        if (_icNetworking != null) {
            return _icNetworking!!
        }
        _icNetworking = Builder(
            name = "IcNetworking", defaultWidth = 800.0.dp, defaultHeight =
            800.0.dp, viewportWidth = 16.0f, viewportHeight = 16.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF2e3436)), stroke = null, fillAlpha = 0.34902f,
                strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(12.0f, 1.0f)
                curveToRelative(0.2656f, 0.0f, 0.5195f, 0.1055f, 0.707f, 0.293f)
                lineToRelative(3.0f, 3.0f)
                curveToRelative(0.3906f, 0.3906f, 0.3906f, 1.0234f, 0.0f, 1.4141f)
                lineToRelative(-3.0f, 3.0f)
                curveToRelative(-0.3906f, 0.3906f, -1.0234f, 0.3906f, -1.4141f, 0.0f)
                reflectiveCurveToRelative(-0.3906f, -1.0234f, 0.0f, -1.4141f)
                lineToRelative(1.293f, -1.293f)
                horizontalLineToRelative(-7.5859f)
                curveToRelative(-0.5508f, 0.0f, -1.0f, -0.4492f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.4492f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(7.5859f)
                lineToRelative(-1.293f, -1.293f)
                curveToRelative(-0.3906f, -0.3906f, -0.3906f, -1.0234f, 0.0f, -1.4141f)
                curveToRelative(0.1875f, -0.1875f, 0.4414f, -0.293f, 0.707f, -0.293f)
                close()
                moveTo(12.0f, 1.0f)
            }
            path(
                fill = SolidColor(Color(0xFF2e3436)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(4.0f, 15.0f)
                curveToRelative(-0.2656f, 0.0f, -0.5195f, -0.1055f, -0.707f, -0.293f)
                lineToRelative(-3.0f, -3.0f)
                curveToRelative(-0.3906f, -0.3906f, -0.3906f, -1.0234f, 0.0f, -1.4141f)
                lineToRelative(3.0f, -3.0f)
                curveToRelative(0.3906f, -0.3906f, 1.0234f, -0.3906f, 1.4141f, 0.0f)
                reflectiveCurveToRelative(0.3906f, 1.0234f, 0.0f, 1.4141f)
                lineToRelative(-1.293f, 1.293f)
                horizontalLineToRelative(7.5859f)
                curveToRelative(0.5508f, 0.0f, 1.0f, 0.4492f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.4492f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-7.5859f)
                lineToRelative(1.293f, 1.293f)
                curveToRelative(0.3906f, 0.3906f, 0.3906f, 1.0234f, 0.0f, 1.4141f)
                curveToRelative(-0.1875f, 0.1875f, -0.4414f, 0.293f, -0.707f, 0.293f)
                close()
                moveTo(4.0f, 15.0f)
            }
        }
            .build()
        return _icNetworking!!
    }

private var _icNetworking: ImageVector? = null
