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
    // MEMORY
    // ================================================================

    /** Memory monitoring feature colors. */
    object Memory {
        val heapUsed = Palette.Green500
        val heapFree = Palette.GreenLight
        val heapTotal = Palette.Green300
        val nativeHeap = Palette.Blue500
    }

    // ================================================================
    // CPU
    // ================================================================

    /** CPU monitoring feature colors. */
    object Cpu {
        val usage = Palette.Blue500
        val usageLight = Palette.BlueLight
        val coreColors = listOf(
            Palette.Blue500,
            Palette.Green500,
            Palette.Orange500,
            Palette.Purple500,
            Palette.Cyan500,
            Palette.Pink600,
            Palette.Gray700,
            Palette.Brown500,
        )

        /** Returns a color for a specific CPU core index, cycling through available colors. */
        fun forCore(index: Int): Color = coreColors[index % coreColors.size]
    }

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
        val sent = Palette.Blue600
        val received = Palette.Green600
        val textMessage = Palette.Gray875
        val binaryMessage = Palette.Purple700
        val pingPong = Palette.Cyan700
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
        val userPreset = Palette.Purple500
        val coordinate = Palette.Cyan500
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
        val diskRead = Palette.Blue500
        val diskWrite = Palette.Blue800
        val network = Palette.Orange500
        val slowCall = Palette.Red550
        val customSlowCode = Palette.Purple500
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
        val encryptedPrefs = Palette.DeepPurpleA200
        val keystore = Palette.Blue500
        val datastore = Palette.Teal700
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

        /** Returns the foreground color for the given [LogLevel]. */
        fun forLevel(level: com.azikar24.wormaceptor.domain.entities.LogLevel): Color = when (level) {
            com.azikar24.wormaceptor.domain.entities.LogLevel.VERBOSE -> verbose
            com.azikar24.wormaceptor.domain.entities.LogLevel.DEBUG -> debug
            com.azikar24.wormaceptor.domain.entities.LogLevel.INFO -> info
            com.azikar24.wormaceptor.domain.entities.LogLevel.WARN -> warn
            com.azikar24.wormaceptor.domain.entities.LogLevel.ERROR -> error
            com.azikar24.wormaceptor.domain.entities.LogLevel.ASSERT -> assert
        }
    }

    // ================================================================
    // FPS
    // ================================================================

    /** FPS monitoring feature colors (light/dark chart variants). */
    object Fps {
        val good = Palette.Emerald500
        val warning = Palette.Amber500
        val critical = Palette.Red500

        /** Chart line color (theme-dependent). */
        @Composable
        fun chartLine(darkTheme: Boolean = isSystemInDarkTheme()): Color =
            if (darkTheme) Palette.Blue300 else Palette.Blue600

        /** Jank indicator color (theme-dependent). */
        @Composable
        fun jankIndicator(darkTheme: Boolean = isSystemInDarkTheme()): Color =
            if (darkTheme) Palette.Pink300 else Palette.Pink600
    }

    // ================================================================
    // RATE LIMIT
    // ================================================================

    /** Network rate-limiting feature colors with light/dark pairs. */
    object RateLimit {

        @Immutable
        data class Scheme(
            val primary: Color,
            val download: Color,
            val upload: Color,
            val latency: Color,
            val packetLoss: Color,
            val enabled: Color,
            val disabled: Color,
            val presetWifi: Color,
            val preset3G: Color,
            val preset2G: Color,
            val presetEdge: Color,
            val presetOffline: Color,
            val cardBackground: Color,
            val sliderTrack: Color,
            val sliderThumb: Color,
            val labelPrimary: Color,
            val labelSecondary: Color,
            val valuePrimary: Color,
        )

        val light = Scheme(
            primary = Palette.DeepPurpleA200,
            download = Palette.Blue500,
            upload = Palette.Green500,
            latency = Palette.Orange500,
            packetLoss = Palette.Red550,
            enabled = Palette.Green500,
            disabled = Palette.Gray450,
            presetWifi = Palette.Green500,
            preset3G = Palette.Blue500,
            preset2G = Palette.Orange500,
            presetEdge = Palette.DeepOrange500,
            presetOffline = Palette.Red550,
            cardBackground = Palette.Gray50,
            sliderTrack = Palette.Gray200,
            sliderThumb = Palette.DeepPurpleA200,
            labelPrimary = Palette.Gray900,
            labelSecondary = Palette.Gray600,
            valuePrimary = Palette.Gray875,
        )

        val dark = Scheme(
            primary = Palette.DeepPurpleA100,
            download = Palette.Blue300,
            upload = Palette.Green300,
            latency = Palette.Orange300,
            packetLoss = Palette.Red300,
            enabled = Palette.Green300,
            disabled = Palette.Gray600,
            presetWifi = Palette.Green300,
            preset3G = Palette.Blue300,
            preset2G = Palette.Orange300,
            presetEdge = Palette.DeepOrange300,
            presetOffline = Palette.Red300,
            cardBackground = Palette.Gray950,
            sliderTrack = Palette.Gray875,
            sliderThumb = Palette.DeepPurpleA100,
            labelPrimary = Palette.Gray200,
            labelSecondary = Palette.Gray450,
            valuePrimary = Palette.Gray300,
        )

        /** Returns the appropriate scheme for the current theme. */
        @Composable
        fun scheme(darkTheme: Boolean = isSystemInDarkTheme()): Scheme = if (darkTheme) dark else light
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
                else -> Colors.Status.grey
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
            val primary: Color,
            val networking: Color,
            val dependencyInjection: Color,
            val uiFramework: Color,
            val imageLoading: Color,
            val serialization: Color,
            val database: Color,
            val reactive: Color,
            val logging: Color,
            val analytics: Color,
            val testing: Color,
            val security: Color,
            val utility: Color,
            val androidx: Color,
            val kotlin: Color,
            val other: Color,
            val versionDetected: Color,
            val versionUnknown: Color,
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
            val versionText: Color,
            val divider: Color,
            val link: Color,
        )

        val light = Scheme(
            primary = Palette.DeepPurple,
            networking = Palette.Blue500,
            dependencyInjection = Palette.Purple500,
            uiFramework = Palette.Green500,
            imageLoading = Palette.Cyan500,
            serialization = Palette.Orange500,
            database = Palette.Amber600,
            reactive = Palette.Pink600,
            logging = Palette.Brown500,
            analytics = Palette.Indigo500,
            testing = Palette.Teal700,
            security = Palette.Red550,
            utility = Palette.Gray700,
            androidx = Palette.BlueLightMaterial,
            kotlin = Palette.DeepPurpleA200,
            other = Palette.Gray450,
            versionDetected = Palette.Green500,
            versionUnknown = Palette.Orange500,
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
            versionText = Palette.Green800,
            divider = Palette.Gray200,
            link = Palette.Blue600,
        )

        val dark = Scheme(
            primary = Palette.PurpleBright,
            networking = Palette.Blue300,
            dependencyInjection = Palette.Purple300,
            uiFramework = Palette.Green300,
            imageLoading = Palette.Cyan300,
            serialization = Palette.Orange300,
            database = Palette.Amber300,
            reactive = Palette.Pink300,
            logging = Palette.Brown300,
            analytics = Palette.Indigo300,
            testing = Palette.Teal300,
            security = Palette.Red300,
            utility = Palette.BlueGrey300,
            androidx = Palette.BlueLightMaterial300,
            kotlin = Palette.DeepPurpleA100,
            other = Palette.Gray300,
            versionDetected = Palette.Green300,
            versionUnknown = Palette.Orange300,
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
            versionText = Palette.Green200,
            divider = Palette.Gray875,
            link = Palette.Blue300,
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
            val primary: Color,
            val nativeSo: Color,
            val dex: Color,
            val jar: Color,
            val aarResource: Color,
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
            primary = Palette.DeepPurpleA200,
            nativeSo = Palette.Blue500,
            dex = Palette.Purple500,
            jar = Palette.Orange500,
            aarResource = Palette.Cyan500,
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
            primary = Palette.DeepPurpleA100,
            nativeSo = Palette.Blue300,
            dex = Palette.Purple300,
            jar = Palette.Orange300,
            aarResource = Palette.Cyan300,
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

    /** File browser feature colors with light/dark pairs. */
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

        @Immutable
        data class FileTypeScheme(
            val folder: Color,
            val image: Color,
            val text: Color,
            val database: Color,
            val other: Color,
        )

        val lightFileType = FileTypeScheme(
            folder = Palette.Orange400,
            image = Palette.Blue400,
            text = Palette.Green400,
            database = Palette.Purple500,
            other = Palette.Gray575,
        )

        val darkFileType = FileTypeScheme(
            folder = Palette.Orange200,
            image = Palette.Blue200,
            text = Palette.Green200,
            database = Palette.Purple200,
            other = Palette.Gray350,
        )

        /** Returns the appropriate syntax color scheme for the current theme. */
        @Composable
        fun syntaxScheme(darkTheme: Boolean = isSystemInDarkTheme()): SyntaxScheme =
            if (darkTheme) darkSyntax else lightSyntax

        /** Returns the appropriate file type color scheme for the current theme. */
        @Composable
        fun fileTypeScheme(darkTheme: Boolean = isSystemInDarkTheme()): FileTypeScheme =
            if (darkTheme) darkFileType else lightFileType
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

        object Template {
            val preset = Palette.Green300
            val user = Palette.Blue300
            val action = Palette.Purple300
        }
    }
}
