/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic status colors used across WormaCeptor features.
 * These colors provide consistent meaning for status indicators.
 *
 * Design Philosophy: Inspired by Linear, Notion, and Stripe
 * - Muted, professional colors that don't overwhelm
 * - Sufficient contrast for accessibility (WCAG 2.1 AA)
 * - Consistent semantic meaning across the app
 */
object WormaCeptorColors {

    // ============================================================
    // STATUS COLORS - Primary semantic indicators
    // ============================================================

    /** Success, healthy, completed, enabled states */
    val StatusGreen = Color(0xFF10B981) // Emerald-500 - Modern, vibrant

    /** Warning, pending, caution states */
    val StatusAmber = Color(0xFFF59E0B) // Amber-500 - Clear visibility

    /** Error, failure, critical, destructive states */
    val StatusRed = Color(0xFFEF4444) // Red-500 - Attention-grabbing

    /** Info, in-progress, selected, link states */
    val StatusBlue = Color(0xFF3B82F6) // Blue-500 - Professional

    /** Disabled, inactive, neutral states */
    val StatusGrey = Color(0xFF6B7280) // Gray-500 - Subtle but readable

    // ============================================================
    // SEMANTIC SURFACE COLORS - For backgrounds and containers
    // ============================================================

    object Surfaces {
        /** Success surface tint */
        val SuccessSubtle = Color(0xFF10B981).copy(alpha = 0.08f)
        val SuccessLight = Color(0xFF10B981).copy(alpha = 0.12f)
        val SuccessMedium = Color(0xFF10B981).copy(alpha = 0.20f)

        /** Warning surface tint */
        val WarningSubtle = Color(0xFFF59E0B).copy(alpha = 0.08f)
        val WarningLight = Color(0xFFF59E0B).copy(alpha = 0.12f)
        val WarningMedium = Color(0xFFF59E0B).copy(alpha = 0.20f)

        /** Error surface tint */
        val ErrorSubtle = Color(0xFFEF4444).copy(alpha = 0.08f)
        val ErrorLight = Color(0xFFEF4444).copy(alpha = 0.12f)
        val ErrorMedium = Color(0xFFEF4444).copy(alpha = 0.20f)

        /** Info surface tint */
        val InfoSubtle = Color(0xFF3B82F6).copy(alpha = 0.08f)
        val InfoLight = Color(0xFF3B82F6).copy(alpha = 0.12f)
        val InfoMedium = Color(0xFF3B82F6).copy(alpha = 0.20f)

        /** Neutral surface tint */
        val NeutralSubtle = Color(0xFF6B7280).copy(alpha = 0.08f)
        val NeutralLight = Color(0xFF6B7280).copy(alpha = 0.12f)
        val NeutralMedium = Color(0xFF6B7280).copy(alpha = 0.20f)
    }

    // ============================================================
    // CHART COLORS - For data visualization
    // ============================================================

    object Chart {
        /** Fast/Good performance */
        val Fast = Color(0xFF10B981) // Emerald

        /** Medium/Acceptable performance */
        val Medium = Color(0xFFF59E0B) // Amber

        /** Slow/Poor performance */
        val Slow = Color(0xFFEF4444) // Red

        /** 2xx Success responses */
        val Success2xx = Color(0xFF10B981)

        /** 3xx Redirect responses */
        val Redirect3xx = Color(0xFF3B82F6)

        /** 4xx Client error responses */
        val ClientError4xx = Color(0xFFF59E0B)

        /** 5xx Server error responses */
        val ServerError5xx = Color(0xFFEF4444)

        /** Pending/Unknown responses */
        val Pending = Color(0xFF6B7280)
    }

    // ============================================================
    // CATEGORY COLORS - For tool groupings
    // ============================================================

    object Category {
        /** Inspection tools - Indigo (analytical, detailed) */
        val Inspection = Color(0xFF6366F1)

        /** Performance tools - Amber (speed, optimization) */
        val Performance = Color(0xFFF59E0B)

        /** Network tools - Emerald (data flow, connectivity) */
        val Network = Color(0xFF10B981)

        /** Simulation tools - Purple (testing, mocking) */
        val Simulation = Color(0xFF8B5CF6)

        /** Core tools - Blue (essential, foundational) */
        val Core = Color(0xFF3B82F6)

        /** Favorites - Amber (highlighted, important) */
        val Favorites = Color(0xFFF59E0B)

        /** Fallback/default category */
        val Fallback = Color(0xFF6B7280)
    }

    // ============================================================
    // CONTENT TYPE COLORS - For data format indicators
    // ============================================================

    object ContentType {
        /** JSON data */
        val Json = Color(0xFFF59E0B) // Amber

        /** XML data */
        val Xml = Color(0xFF8B5CF6) // Purple

        /** HTML content */
        val Html = Color(0xFFEC4899) // Pink

        /** Protocol Buffers */
        val Protobuf = Color(0xFF10B981) // Emerald

        /** Form data */
        val FormData = Color(0xFF3B82F6) // Blue

        /** Multipart form data */
        val Multipart = Color(0xFF6366F1) // Indigo

        /** Plain text */
        val PlainText = Color(0xFF6B7280) // Gray

        /** Binary data */
        val Binary = Color(0xFFEF4444) // Red

        /** PDF documents */
        val Pdf = Color(0xFFDC2626) // Red-600

        /** Image content */
        val Image = Color(0xFF14B8A6) // Teal

        /** Unknown content type */
        val Unknown = Color(0xFF9CA3AF) // Gray-400
    }

    // ============================================================
    // HTTP METHOD COLORS - For request method indicators
    // ============================================================

    object HttpMethod {
        /** GET request - retrieval */
        val Get = Color(0xFF3B82F6) // Blue

        /** POST request - creation */
        val Post = Color(0xFF10B981) // Green

        /** PUT request - replacement */
        val Put = Color(0xFFF59E0B) // Amber

        /** PATCH request - modification */
        val Patch = Color(0xFF8B5CF6) // Purple

        /** DELETE request - removal */
        val Delete = Color(0xFFEF4444) // Red

        /** HEAD request - metadata */
        val Head = Color(0xFF6B7280) // Gray

        /** OPTIONS request - capabilities */
        val Options = Color(0xFF6366F1) // Indigo

        /** CONNECT request - tunnel */
        val Connect = Color(0xFF14B8A6) // Teal

        /** TRACE request - debugging */
        val Trace = Color(0xFF9CA3AF) // Gray-400

        /** Get color for HTTP method */
        fun forMethod(method: String): Color = when (method.uppercase()) {
            "GET" -> Get
            "POST" -> Post
            "PUT" -> Put
            "PATCH" -> Patch
            "DELETE" -> Delete
            "HEAD" -> Head
            "OPTIONS" -> Options
            "CONNECT" -> Connect
            "TRACE" -> Trace
            else -> Get
        }
    }

    // ============================================================
    // ACCENT COLORS - For highlights and emphasis
    // ============================================================

    object Accent {
        /** Primary accent - vibrant blue */
        val Primary = Color(0xFF0061A4)

        /** Secondary accent - muted purple */
        val Secondary = Color(0xFF6B5778)

        /** Tertiary accent - teal */
        val Tertiary = Color(0xFF14B8A6)

        /** Highlight color for search results */
        val Highlight = Color(0xFFFBBF24) // Yellow-400

        /** Selection color */
        val Selection = Color(0xFF3B82F6).copy(alpha = 0.20f)

        /** Focus ring color */
        val FocusRing = Color(0xFF3B82F6).copy(alpha = 0.40f)
    }

    // ============================================================
    // FEATURE-SPECIFIC COLORS
    // Consolidated from individual feature design systems
    // ============================================================

    /**
     * Memory monitoring feature colors.
     */
    object Memory {
        val HeapUsed = Color(0xFF4CAF50) // Green
        val HeapFree = Color(0xFFC8E6C9) // Light Green
        val HeapTotal = Color(0xFF81C784) // Medium Green
        val NativeHeap = Color(0xFF2196F3) // Blue
    }

    /**
     * CPU monitoring feature colors.
     */
    object Cpu {
        val Usage = Color(0xFF2196F3) // Blue
        val UsageLight = Color(0xFFBBDEFB) // Light Blue

        /** Per-core colors for multi-core visualization */
        val CoreColors = listOf(
            Color(0xFF2196F3), // Blue
            Color(0xFF4CAF50), // Green
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0), // Purple
            Color(0xFF00BCD4), // Cyan
            Color(0xFFE91E63), // Pink
            Color(0xFF607D8B), // Blue Grey
            Color(0xFF795548), // Brown
        )

        fun forCore(index: Int): Color = CoreColors[index % CoreColors.size]
    }

    /**
     * WebSocket feature colors.
     */
    object WebSocket {
        // Connection states
        val Connecting = Color(0xFFF57C00) // Orange
        val Open = Color(0xFF388E3C) // Green
        val Closing = Color(0xFFE65100) // Deep Orange
        val Closed = Color(0xFF757575) // Gray

        // Message directions
        val Sent = Color(0xFF1976D2) // Blue
        val Received = Color(0xFF388E3C) // Green

        // Message types
        val TextMessage = Color(0xFF424242) // Gray
        val BinaryMessage = Color(0xFF7B1FA2) // Purple
        val PingPong = Color(0xFF0097A7) // Cyan
    }

    /**
     * Database feature colors.
     */
    object Database {
        // Data type colors
        val Integer = Color(0xFF4FC3F7) // Light Blue
        val Real = Color(0xFF81C784) // Light Green
        val Text = Color(0xFFFFB74D) // Orange
        val Blob = Color(0xFFBA68C8) // Purple
        val NullValue = Color(0xFF90A4AE) // Blue Grey
        val PrimaryKey = Color(0xFFFFD54F) // Amber

        // SQL syntax colors
        val SqlKeyword = Color(0xFF569CD6) // Blue
        val SqlString = Color(0xFFCE9178) // Orange
        val SqlNumber = Color(0xFFB5CEA8) // Green
        val SqlFunction = Color(0xFFDCDCAA) // Yellow
        val SqlOperator = Color(0xFFD4D4D4) // Light Grey
        val SqlComment = Color(0xFF6A9955) // Green
        val SqlTable = Color(0xFF4EC9B0) // Cyan

        fun forDataType(type: String): Color = when (type.uppercase()) {
            "INTEGER", "INT", "BIGINT", "SMALLINT", "TINYINT" -> Integer
            "REAL", "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC" -> Real
            "TEXT", "VARCHAR", "CHAR", "CLOB" -> Text
            "BLOB", "BINARY", "VARBINARY" -> Blob
            else -> Text
        }
    }

    /**
     * Cookies feature colors.
     */
    object Cookies {
        val Secure = Color(0xFF4CAF50) // Green
        val HttpOnly = Color(0xFF2196F3) // Blue
        val Session = Color(0xFF9C27B0) // Purple
        val Expired = Color(0xFFF44336) // Red
        val Valid = Color(0xFF4CAF50) // Green
        val Domain = Color(0xFFFF9800) // Orange
        val SameSiteStrict = Color(0xFF3F51B5) // Indigo
        val SameSiteLax = Color(0xFF00BCD4) // Cyan
        val SameSiteNone = Color(0xFFFF5722) // Deep Orange
    }

    /**
     * Location simulation feature colors.
     */
    object Location {
        val Enabled = Color(0xFF4CAF50) // Green - mock active
        val Disabled = Color(0xFF9E9E9E) // Gray - mock inactive
        val Warning = Color(0xFFFF9800) // Orange
        val Error = Color(0xFFF44336) // Red
        val BuiltInPreset = Color(0xFF2196F3) // Blue
        val UserPreset = Color(0xFF9C27B0) // Purple
        val Coordinate = Color(0xFF00BCD4) // Cyan
    }

    /**
     * Leak detection feature colors.
     */
    object LeakDetection {
        val Critical = Color(0xFFD32F2F) // Red 700
        val High = Color(0xFFE65100) // Orange 800
        val Medium = Color(0xFFF9A825) // Yellow 800
        val Low = Color(0xFF00838F) // Cyan 800
        val Monitoring = Color(0xFF4CAF50) // Green
        val Idle = Color(0xFF9E9E9E) // Grey
    }

    /**
     * Thread violation feature colors.
     */
    object ThreadViolation {
        val DiskRead = Color(0xFF2196F3) // Blue
        val DiskWrite = Color(0xFF1565C0) // Blue 800
        val Network = Color(0xFFFF9800) // Orange
        val SlowCall = Color(0xFFF44336) // Red
        val CustomSlowCode = Color(0xFF9C27B0) // Purple
        val Monitoring = Color(0xFF4CAF50) // Green
        val Idle = Color(0xFF9E9E9E) // Grey
    }

    /**
     * Secure storage feature colors.
     */
    object SecureStorage {
        val Primary = Color(0xFF5C6BC0) // Indigo
        val Encrypted = Color(0xFF4CAF50) // Green
        val Unencrypted = Color(0xFFFF9800) // Orange
        val EncryptedPrefs = Color(0xFF7C4DFF) // Deep Purple
        val Keystore = Color(0xFF2196F3) // Blue
        val Datastore = Color(0xFF009688) // Teal
    }

    /**
     * File browser feature colors.
     */
    object FileBrowser {
        val Folder = Color(0xFFFFC107) // Amber
        val File = Color(0xFF2196F3) // Blue
        val Image = Color(0xFF4CAF50) // Green
        val Video = Color(0xFFE91E63) // Pink
        val Audio = Color(0xFF9C27B0) // Purple
        val Document = Color(0xFFFF5722) // Deep Orange
        val Archive = Color(0xFF795548) // Brown
        val Code = Color(0xFF00BCD4) // Cyan
        val Database = Color(0xFF3F51B5) // Indigo
    }

    /**
     * Preferences viewer feature colors.
     */
    object Preferences {
        val StringValue = Color(0xFFFF9800) // Orange
        val IntValue = Color(0xFF2196F3) // Blue
        val BooleanTrue = Color(0xFF4CAF50) // Green
        val BooleanFalse = Color(0xFFF44336) // Red
        val FloatValue = Color(0xFF9C27B0) // Purple
        val LongValue = Color(0xFF00BCD4) // Cyan
        val SetValue = Color(0xFF607D8B) // Blue Grey
    }

    // ============================================================
    // DEPRECATED - Use new semantic colors instead
    // ============================================================

    @Deprecated("Use ContentType.Xml or another appropriate color")
    val ContentPurple = Color(0xFF6B5778)

    @Deprecated("Use ContentType.Image")
    val ContentCyan = Color(0xFF00838F)
}
