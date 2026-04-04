package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.ui.graphics.Color

/**
 * Raw hex color palette for the entire WormaCeptor design system.
 * Every unique color value used across the codebase is defined here exactly once.
 *
 * Naming follows Tailwind-style color families (e.g., Emerald500, Red700).
 * All other token files reference [Palette] entries instead of hardcoded hex values.
 */
@Suppress("MagicNumber")
internal object Palette {

    // ============================================================
    // WHITE / BLACK
    // ============================================================
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // ============================================================
    // GRAY
    // ============================================================
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray150 = Color(0xFFF0F0F0)
    val Gray200 = Color(0xFFE0E0E0)
    val Gray300 = Color(0xFFBDBDBD)
    val Gray350 = Color(0xFFB0BEC5) // Blue Grey 200
    val Gray400 = Color(0xFF9CA3AF)
    val Gray450 = Color(0xFF9E9E9E) // Grey 500
    val Gray500 = Color(0xFF8A8A8A)
    val Gray525 = Color(0xFF8C8C8C)
    val Gray550 = Color(0xFF808080)
    val Gray575 = Color(0xFF78909C) // Blue Grey 400
    val Gray600 = Color(0xFF757575)
    val Gray625 = Color(0xFF6B7280) // Gray-500 (Tailwind)
    val Gray650 = Color(0xFF6B6B6B)
    val Gray675 = Color(0xFF616161)
    val Gray700 = Color(0xFF607D8B) // Blue Grey 500
    val Gray750 = Color(0xFF606060)
    val Gray800 = Color(0xFF525252)
    val Gray850 = Color(0xFF455A64) // Blue Grey 700
    val Gray875 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)
    val Gray925 = Color(0xFF1F1F1F)
    val Gray950 = Color(0xFF1E1E1E)
    val Gray960 = Color(0xFF2D2D2D)
    val Gray975 = Color(0xFF141414)
    val Gray980 = Color(0xFF252526)
    val Gray990 = Color(0xFF0A0A0A)

    // ============================================================
    // BLUE GREY
    // ============================================================
    val BlueGrey300 = Color(0xFF90A4AE)

    // ============================================================
    // RED
    // ============================================================
    val Red300 = Color(0xFFE57373)
    val Red400 = Color(0xFFEF5350)
    val Red500 = Color(0xFFEF4444)
    val Red550 = Color(0xFFF44336)
    val Red600 = Color(0xFFDC2626)
    val Red700 = Color(0xFFD32F2F)
    val Red800 = Color(0xFFF87171) // light red variant used in dark error

    // ============================================================
    // PINK
    // ============================================================
    val Pink300 = Color(0xFFF06292)
    val Pink500 = Color(0xFFEC4899)
    val Pink600 = Color(0xFFE91E63)
    val PinkRose = Color(0xFFC2185B) // Assert color

    // ============================================================
    // ORANGE
    // ============================================================
    val Orange200 = Color(0xFFFFCC80)
    val Orange300 = Color(0xFFFFB74D)
    val Orange400 = Color(0xFFFFA726)
    val Orange500 = Color(0xFFFF9800)
    val DeepOrange300 = Color(0xFFFF8A65)
    val DeepOrange500 = Color(0xFFFF5722)
    val DeepOrange600 = Color(0xFFE64A19)
    val DeepOrange700 = Color(0xFFF57C00)
    val DeepOrange800 = Color(0xFFE65100)
    val DeepOrange900 = Color(0xFF613214) // Dark search highlight

    // ============================================================
    // AMBER
    // ============================================================
    val Amber300 = Color(0xFFFFD54F)
    val Amber400 = Color(0xFFFDD835)
    val Amber500 = Color(0xFFF59E0B)
    val Amber600 = Color(0xFFFFC107)
    val Amber700 = Color(0xFFD97706)
    val AmberHighlight = Color(0xFFFBBF24) // Yellow-400 highlight

    // ============================================================
    // YELLOW
    // ============================================================
    val Yellow200 = Color(0xFFFFF59D) // Light search highlight
    val Yellow500 = Color(0xFFFFEB3B)
    val YellowSyntax = Color(0xFFDCDCAA)
    val Yellow800 = Color(0xFFF9A825)

    // ============================================================
    // GREEN
    // ============================================================
    val Green200 = Color(0xFFA5D6A7)
    val Green300 = Color(0xFF81C784)
    val Green400 = Color(0xFF66BB6A)
    val Green500 = Color(0xFF4CAF50)
    val Green600 = Color(0xFF388E3C)
    val Green700 = Color(0xFF16A34A) // Success semantic
    val Green800 = Color(0xFF1B5E20)
    val GreenLight = Color(0xFFC8E6C9) // HeapFree

    // ============================================================
    // EMERALD
    // ============================================================
    val Emerald500 = Color(0xFF10B981)

    // ============================================================
    // TEAL
    // ============================================================
    val Teal500 = Color(0xFF14B8A6)
    val Teal600 = Color(0xFF0D9488) // Accent light
    val Teal300 = Color(0xFF4DB6AC) // Teal 300
    val Teal700 = Color(0xFF009688) // Teal 500 material
    val TealBright = Color(0xFF2DD4BF) // Accent dark

    // ============================================================
    // CYAN
    // ============================================================
    val Cyan300 = Color(0xFF4DD0E1)
    val Cyan500 = Color(0xFF00BCD4)
    val Cyan700 = Color(0xFF0097A7)
    val Cyan800 = Color(0xFF00838F)
    val CyanSyntax = Color(0xFF4EC9B0)

    // ============================================================
    // BLUE
    // ============================================================
    val Blue200 = Color(0xFF90CAF9)
    val Blue300 = Color(0xFF64B5F6)
    val Blue400 = Color(0xFF42A5F5)
    val Blue500 = Color(0xFF2196F3)
    val Blue600 = Color(0xFF1976D2)
    val Blue700 = Color(0xFF3B82F6) // Blue-500 (Tailwind)
    val Blue800 = Color(0xFF1565C0)
    val BlueLightMaterial = Color(0xFF03A9F4) // Light Blue 500
    val BlueLightMaterial300 = Color(0xFF4FC3F7) // Light Blue 300
    val BlueDarkSearch = Color(0xFF264F78) // Dark search highlight current
    val BlueLight = Color(0xFFBBDEFB) // CpuUsageLight

    // ============================================================
    // INDIGO
    // ============================================================
    val Indigo300 = Color(0xFF7986CB)
    val Indigo500 = Color(0xFF3F51B5)
    val Indigo600 = Color(0xFF6366F1) // Indigo (Tailwind)
    val IndigoMaterial = Color(0xFF5C6BC0) // Indigo 400

    // ============================================================
    // PURPLE
    // ============================================================
    val Purple200 = Color(0xFFCE93D8)
    val Purple300 = Color(0xFFBA68C8)
    val Purple500 = Color(0xFF9C27B0)
    val Purple600 = Color(0xFF8B5CF6) // Violet (Tailwind)
    val Purple700 = Color(0xFF7B1FA2) // Purple 700
    val PurpleBright = Color(0xFFBB86FC) // Purple 200 (Material Dark)
    val DeepPurple = Color(0xFF6200EE) // Deep Purple
    val DeepPurpleA100 = Color(0xFFB388FF)
    val DeepPurpleA200 = Color(0xFF7C4DFF)
    val DeepPurpleA300 = Color(0xFF9876AA) // Dark syntax property

    // ============================================================
    // BROWN
    // ============================================================
    val Brown300 = Color(0xFFA1887F)
    val Brown500 = Color(0xFF795548)

    // ============================================================
    // SYNTAX HIGHLIGHTING — LIGHT THEME
    // ============================================================
    val SyntaxLightKeyword = Color(0xFF0033B3)
    val SyntaxLightString = Color(0xFF067D17)
    val SyntaxLightNumber = Color(0xFF1750EB)
    val SyntaxLightProperty = Color(0xFF871094)
    val SyntaxLightTypeAnnotation = Color(0xFF008080)
    val SyntaxLightJsonString = Color(0xFF2E7D32)

    // ============================================================
    // SYNTAX HIGHLIGHTING — DARK THEME
    // ============================================================
    val SyntaxDarkKeyword = Color(0xFFCC7832)
    val SyntaxDarkString = Color(0xFF6A8759)
    val SyntaxDarkNumber = Color(0xFF6897BB)
    val SyntaxDarkOperator = Color(0xFFA9B7C6)
    val SyntaxDarkTag = Color(0xFFE8BF6A)

    // ============================================================
    // DATABASE SYNTAX
    // ============================================================
    val SqlKeyword = Color(0xFF569CD6)
    val SqlString = Color(0xFFCE9178)
    val SqlNumber = Color(0xFFB5CEA8)
    val SqlComment = Color(0xFF6A9955)
    val SqlOperatorLight = Color(0xFFD4D4D4) // Light grey SQL operator

    // ============================================================
    // OVERLAY (fixed, not theme-aware)
    // ============================================================
    val OverlayBackground = Color(0xFF1C1C1E)
    val OverlayGreen = Color(0xFF32D74B)
    val OverlayAmber = Color(0xFFFF9F0A)
    val OverlayRed = Color(0xFFFF453A)
    val OverlayGray = Color(0xFF8E8E93)

    // ============================================================
    // ACCENT TINTS (with alpha encoded in ARGB)
    // ============================================================
    val AccentSubtleLight = Color(0x120D9488)
    val AccentSubtleDark = Color(0x152DD4BF)
}
