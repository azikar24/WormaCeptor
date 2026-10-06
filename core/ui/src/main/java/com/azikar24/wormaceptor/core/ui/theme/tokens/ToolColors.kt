@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")

package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Feature/tool-specific color groups for individual tools and inspectors.
 * Every hex value references [Palette] -- no hardcoded literals allowed.
 */
object ToolColors {

    // ================================================================
    // DATABASE
    // ================================================================

    /** Database feature colors. */
    object Database {
        val integer = Palette.BlueLightMaterial300
        val real = Palette.Green300
        val text = Palette.Orange300
        val blob = Palette.Purple300
        val nullValue = Palette.BlueGrey300
        val primaryKey = Palette.Amber300
        val sqlKeyword = Palette.SqlKeyword
        val sqlString = Palette.SqlString
        val sqlNumber = Palette.SqlNumber
        val sqlFunction = Palette.YellowSyntax
        val sqlOperator = Palette.SqlOperatorLight
        val sqlComment = Palette.SqlComment
        val sqlTable = Palette.CyanSyntax

        /** Returns a color for a given SQL data type name. */
        fun forDataType(type: String): Color = when (type.uppercase()) {
            "INTEGER", "INT", "BIGINT", "SMALLINT", "TINYINT" -> integer
            "REAL", "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC" -> real
            "TEXT", "VARCHAR", "CHAR", "CLOB" -> text
            "BLOB", "BINARY", "VARBINARY" -> blob
            else -> text
        }
    }

    // ================================================================
    // WEBSOCKET
    // ================================================================

    /** WebSocket feature colors. */
    object WebSocket {
        val connecting = Palette.DeepOrange700
        val open = Palette.Green600
        val closing = Palette.DeepOrange800
        val closed = Palette.Gray600
    }

    // ================================================================
    // LOCATION
    // ================================================================

    /** Location simulation feature colors. */
    object Location {
        val enabled = Palette.Green500
        val disabled = Palette.Gray450
        val warning = Palette.Orange500
        val error = Palette.Red550
        val builtInPreset = Palette.Blue500
    }

    // ================================================================
    // LEAK DETECTION
    // ================================================================

    /** Leak detection feature colors. */
    object LeakDetection {
        val critical = Palette.Red700
        val high = Palette.DeepOrange800
        val medium = Palette.Yellow800
        val low = Palette.Cyan800
        val monitoring = Palette.Green500
        val idle = Palette.Gray450
    }

    // ================================================================
    // THREAD VIOLATION
    // ================================================================

    /** Thread violation detection feature colors. */
    object ThreadViolation {
        val monitoring = Palette.Green500
        val idle = Palette.Gray450
    }

    // ================================================================
    // SECURE STORAGE
    // ================================================================

    /** Secure storage feature colors. */
    object SecureStorage {
        val primary = Palette.IndigoMaterial
        val encrypted = Palette.Green500
        val unencrypted = Palette.Orange500
    }

    // ================================================================
    // LOG LEVEL
    // ================================================================

    /** Log-level foreground colors (backgrounds are derived at call site with alpha). */
    object LogLevel {
        val verbose = Palette.Gray625
        val debug = Palette.Blue700
        val info = Palette.Emerald500
        val warn = Palette.Amber500
        val error = Palette.Red500
        val assert = Palette.PinkRose
    }

    // ================================================================
    // FPS
    // ================================================================

    /** FPS monitoring threshold colors. */
    object Fps {
        val good = Palette.Emerald500
        val warning = Palette.Amber500
        val critical = Palette.Red500
    }

    // ================================================================
    // PREFERENCES
    // ================================================================

    /** Preferences inspector type colors with light/dark pairs. */
    object Preferences {

        @Immutable
        data class TypeScheme(
            val string: Color,
            val int: Color,
            val long: Color,
            val float: Color,
            val boolean: Color,
            val stringSet: Color,
        ) {
            /** Returns the color associated with the given preference value type name. */
            fun forTypeName(typeName: String): Color = when (typeName) {
                "String" -> string
                "Int" -> int
                "Long" -> long
                "Float" -> float
                "Boolean" -> boolean
                "StringSet" -> stringSet
                else -> Palette.Gray625
            }
        }

        val light = TypeScheme(
            string = Palette.Green500,
            int = Palette.Blue500,
            long = Palette.Indigo500,
            float = Palette.Purple500,
            boolean = Palette.Orange500,
            stringSet = Palette.Cyan500,
        )

        val dark = TypeScheme(
            string = Palette.Green300,
            int = Palette.Blue300,
            long = Palette.Indigo300,
            float = Palette.Purple300,
            boolean = Palette.Orange300,
            stringSet = Palette.Cyan300,
        )

        /** Returns the appropriate type scheme for the current theme. */
        @Composable
        fun typeScheme(darkTheme: Boolean = isSystemInDarkTheme()): TypeScheme = if (darkTheme) dark else light
    }

    // ================================================================
    // DEPENDENCIES INSPECTOR
    // ================================================================

    /** Dependencies inspector feature colors with light/dark pairs. */
    object DependenciesInspector {

        @Immutable
        data class Scheme(
            val versionDetected: Color,
            val highConfidence: Color,
            val mediumConfidence: Color,
            val lowConfidence: Color,
            val cardBackground: Color,
            val chipBackground: Color,
            val selectedChipBackground: Color,
            val searchBackground: Color,
            val labelPrimary: Color,
            val labelSecondary: Color,
            val valuePrimary: Color,
            val divider: Color,
        )

        val light = Scheme(
            versionDetected = Palette.Green500,
            highConfidence = Palette.Green500,
            mediumConfidence = Palette.Amber600,
            lowConfidence = Palette.Orange500,
            cardBackground = Palette.Gray50,
            chipBackground = Palette.Gray200,
            selectedChipBackground = Palette.DeepPurple,
            searchBackground = Palette.Gray100,
            labelPrimary = Palette.Gray900,
            labelSecondary = Palette.Gray600,
            valuePrimary = Palette.Gray875,
            divider = Palette.Gray200,
        )

        val dark = Scheme(
            versionDetected = Palette.Green300,
            highConfidence = Palette.Green300,
            mediumConfidence = Palette.Amber300,
            lowConfidence = Palette.Orange300,
            cardBackground = Palette.Gray950,
            chipBackground = Palette.Gray875,
            selectedChipBackground = Palette.DeepPurpleA200,
            searchBackground = Palette.Gray960,
            labelPrimary = Palette.Gray200,
            labelSecondary = Palette.Gray450,
            valuePrimary = Palette.Gray300,
            divider = Palette.Gray875,
        )

        /** Returns the appropriate scheme for the current theme. */
        @Composable
        fun scheme(darkTheme: Boolean = isSystemInDarkTheme()): Scheme = if (darkTheme) dark else light
    }

    // ================================================================
    // LOADED LIBRARIES
    // ================================================================

    /** Loaded libraries inspector feature colors with light/dark pairs. */
    object LoadedLibraries {

        @Immutable
        data class Scheme(
            val systemLibrary: Color,
            val appLibrary: Color,
            val systemBadge: Color,
            val cardBackground: Color,
            val chipBackground: Color,
            val selectedChipBackground: Color,
            val searchBackground: Color,
            val labelPrimary: Color,
            val labelSecondary: Color,
            val pathText: Color,
            val valuePrimary: Color,
            val searchHighlight: Color,
            val divider: Color,
        )

        val light = Scheme(
            systemLibrary = Palette.Gray700,
            appLibrary = Palette.Green500,
            systemBadge = Palette.Gray575,
            cardBackground = Palette.Gray50,
            chipBackground = Palette.Gray200,
            selectedChipBackground = Palette.Blue600,
            searchBackground = Palette.Gray100,
            labelPrimary = Palette.Gray900,
            labelSecondary = Palette.Gray600,
            pathText = Palette.Gray850,
            valuePrimary = Palette.Gray875,
            searchHighlight = Palette.Yellow500,
            divider = Palette.Gray200,
        )

        val dark = Scheme(
            systemLibrary = Palette.BlueGrey300,
            appLibrary = Palette.Green300,
            systemBadge = Palette.BlueGrey300,
            cardBackground = Palette.Gray950,
            chipBackground = Palette.Gray875,
            selectedChipBackground = Palette.Blue800,
            searchBackground = Palette.Gray960,
            labelPrimary = Palette.Gray200,
            labelSecondary = Palette.Gray450,
            pathText = Palette.Gray575,
            valuePrimary = Palette.Gray300,
            searchHighlight = Palette.Amber400,
            divider = Palette.Gray875,
        )

        /** Returns the appropriate scheme for the current theme. */
        @Composable
        fun scheme(darkTheme: Boolean = isSystemInDarkTheme()): Scheme = if (darkTheme) dark else light
    }

    // ================================================================
    // RECOMPOSITION
    // ================================================================

    /** Recomposition tracker feature colors. */
    object Recomposition {
        val normal = Palette.Emerald500
        val elevated = Palette.Amber500
        val excessive = Palette.Orange500
        val critical = Palette.Red500
    }

    // ================================================================
    // FILE BROWSER
    // ================================================================

    /** File browser syntax colors with light/dark pairs. */
    object FileBrowser {

        @Immutable
        data class SyntaxScheme(
            val jsonKey: Color,
            val jsonString: Color,
            val jsonNumber: Color,
            val jsonBoolNull: Color,
            val jsonBracket: Color,
            val xmlTag: Color,
            val xmlAttrName: Color,
            val xmlAttrValue: Color,
            val xmlContent: Color,
            val xmlComment: Color,
        )

        val lightSyntax = SyntaxScheme(
            jsonKey = Palette.Purple500,
            jsonString = Palette.SyntaxLightJsonString,
            jsonNumber = Palette.Blue800,
            jsonBoolNull = Palette.DeepOrange600,
            jsonBracket = Palette.Gray675,
            xmlTag = Palette.Blue800,
            xmlAttrName = Palette.Purple500,
            xmlAttrValue = Palette.SyntaxLightJsonString,
            xmlContent = Palette.Gray900,
            xmlComment = Palette.Gray600,
        )

        val darkSyntax = SyntaxScheme(
            jsonKey = Palette.Purple200,
            jsonString = Palette.Green300,
            jsonNumber = Palette.Blue300,
            jsonBoolNull = Palette.DeepOrange300,
            jsonBracket = Palette.Gray300,
            xmlTag = Palette.Blue300,
            xmlAttrName = Palette.Purple200,
            xmlAttrValue = Palette.Green300,
            xmlContent = Palette.Gray200,
            xmlComment = Palette.Gray450,
        )

        /** Returns the appropriate syntax color scheme for the current theme. */
        @Composable
        fun syntaxScheme(darkTheme: Boolean = isSystemInDarkTheme()): SyntaxScheme =
            if (darkTheme) darkSyntax else lightSyntax
    }

    // ================================================================
    // PUSH SIMULATOR
    // ================================================================

    /** Push notification simulator feature colors. */
    object PushSimulator {

        object Priority {
            val low = Palette.BlueGrey300
            val default = Palette.BlueLightMaterial300
            val high = Palette.Orange300
            val max = Palette.Red400

            /** Returns the color associated with the given notification priority name. */
            fun forPriority(priorityName: String): Color = when (priorityName.uppercase()) {
                "LOW" -> low
                "DEFAULT" -> default
                "HIGH" -> high
                "MAX" -> max
                else -> default
            }
        }
    }

    // ================================================================
    // PERFORMANCE OVERLAY — fixed iOS Dynamic Island style (not theme-aware)
    // ================================================================

    object Overlay {
        val background = Palette.OverlayBackground
        val good = Palette.OverlayGreen
        val warning = Palette.OverlayAmber
        val critical = Palette.OverlayRed
        val inactive = Palette.OverlayGray
        val textPrimary = Palette.White
        val textSecondary = Palette.OverlayGray
    }

    // ================================================================
    // DISMISS ZONE
    // ================================================================

    object DismissZone {
        val error = Palette.Red600
        val surface = Palette.Gray925
        val iconTint = Palette.White
    }

    // ================================================================
    // VIEWER — accent colors for body viewers
    // ================================================================

    object Viewer {
        val protobufAccent = Palette.Purple600
        val onOverlay = Palette.White
    }

    // ================================================================
    // FLOATING BUTTON — platform service overlay
    // Runs in a non-themed WindowManager context, so values are bound
    // statically to the light-mode semantic accent (Teal800).
    // ================================================================

    object FloatingButton {
        val background = Palette.Teal800
        val iconTint = Palette.White
    }
}
