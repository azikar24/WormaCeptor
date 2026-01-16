package com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun WormaceptorLogo(): ImageVector {
    val isDarkTheme = isSystemInDarkTheme()
    return remember(isDarkTheme) {
        Builder(
            name = "IcIconFull",
            defaultWidth = 787.dp,
            defaultHeight = 340.dp,
            viewportWidth = 787f,
            viewportHeight = 340f
        ).apply {
            path(
                fill = SolidColor(
                    if(isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF000000)
                ),
                stroke = null,
                pathFillType = NonZero
            ) {
                moveTo(769.9f, 27.47f)
                curveTo(756.72f, 9.5f, 736.37f, 0f, 711.05f, 0f)
                curveTo(641.65f, 0f, 588.51f, 70.66f, 537.5f, 141.26f)
                curveTo(520.57f, 114.24f, 502.88f, 87.5f, 482.97f, 66.08f)
                curveTo(454.26f, 35.19f, 424.98f, 20.19f, 393.49f, 20.19f)
                curveTo(361.99f, 20.19f, 332.73f, 35.19f, 304.01f, 66.08f)
                curveTo(289.17f, 82.05f, 275.54f, 100.98f, 262.57f, 120.79f)
                lineTo(279.62f, 144.34f)
                curveTo(312.46f, 93.04f, 347.68f, 47.34f, 393.49f, 47.34f)
                curveTo(445.39f, 47.34f, 483.69f, 105.96f, 520.34f, 164.97f)
                lineTo(535.78f, 189.81f)
                curveTo(569.94f, 244.22f, 603.96f, 291.53f, 652.11f, 291.53f)
                curveTo(685.48f, 291.53f, 720.34f, 265.48f, 747.76f, 220.07f)
                curveTo(771.96f, 179.99f, 787f, 129.97f, 787f, 89.57f)
                curveTo(787f, 63.79f, 781.25f, 42.9f, 769.91f, 27.47f)
                close()

                moveTo(724.56f, 206.03f)
                curveTo(702.49f, 242.57f, 675.4f, 264.39f, 652.1f, 264.39f)
                curveTo(615.81f, 264.39f, 585.04f, 217.78f, 552.97f, 166.16f)
                curveTo(553.52f, 165.4f, 554.07f, 164.63f, 554.63f, 163.86f)
                curveTo(603.15f, 96.65f, 653.33f, 27.15f, 711.05f, 27.15f)
                curveTo(743.44f, 27.15f, 759.88f, 48.14f, 759.88f, 89.57f)
                curveTo(759.88f, 125.31f, 746.34f, 169.94f, 724.56f, 206.03f)
                close()

                moveTo(505.97f, 184.4f)
                curveTo(469.62f, 232.65f, 432.23f, 273.85f, 393.49f, 273.85f)
                curveTo(354.74f, 273.85f, 318.59f, 234.01f, 282.82f, 186.81f)
                curveTo(277.41f, 179.66f, 272.02f, 172.35f, 266.64f, 164.98f)
                lineTo(249.48f, 141.26f)
                curveTo(198.46f, 70.66f, 145.32f, 0f, 75.92f, 0f)
                curveTo(50.61f, 0f, 30.25f, 9.5f, 17.07f, 27.47f)
                curveTo(5.74f, 42.9f, 0f, 63.81f, 0f, 89.57f)
                curveTo(0f, 129.97f, 15.03f, 180f, 39.23f, 220.07f)
                curveTo(66.64f, 265.48f, 101.5f, 291.54f, 134.88f, 291.54f)
                curveTo(183.03f, 291.54f, 217.04f, 244.22f, 251.22f, 189.81f)
                curveTo(295.42f, 249.89f, 339.05f, 301f, 393.49f, 301f)
                curveTo(442.18f, 301f, 482.23f, 260.09f, 521.8f, 208.46f)
                lineTo(518.52f, 203.57f)
                lineTo(505.97f, 184.4f)
                close()

                moveTo(134.88f, 264.39f)
                curveTo(111.57f, 264.39f, 84.48f, 242.57f, 62.43f, 206.03f)
                curveTo(40.65f, 169.94f, 27.1f, 125.31f, 27.1f, 89.57f)
                curveTo(27.1f, 48.14f, 43.53f, 27.15f, 75.92f, 27.15f)
                curveTo(133.67f, 27.15f, 183.83f, 96.65f, 232.34f, 163.86f)
                curveTo(232.9f, 164.63f, 233.46f, 165.4f, 234f, 166.16f)
                curveTo(201.93f, 217.78f, 171.17f, 264.39f, 134.88f, 264.39f)
                close()
            }
        }.build()
    }
}
