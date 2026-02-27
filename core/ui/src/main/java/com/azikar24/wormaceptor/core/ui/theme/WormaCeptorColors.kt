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

    /** Success, healthy, completed, and enabled states. */
    val StatusGreen = Color(0xFF10B981) // Emerald-500 - Modern, vibrant

    /** Warning, pending, and caution states. */
    val StatusAmber = Color(0xFFF59E0B) // Amber-500 - Clear visibility

    /** Error, failure, critical, and destructive states. */
    val StatusRed = Color(0xFFEF4444) // Red-500 - Attention-grabbing

    /** Info, in-progress, selected, and link states. */
    val StatusBlue = Color(0xFF3B82F6) // Blue-500 - Professional

    /** Disabled, inactive, and neutral states. */
    val StatusGrey = Color(0xFF6B7280) // Gray-500 - Subtle but readable

    // ============================================================
    // CHART COLORS - For data visualization
    // ============================================================

    /** Colors for data visualizations and chart elements. */
    object Chart {
        /** Fast/Good performance. */
        val Fast = Color(0xFF10B981) // Emerald

        /** Medium/Acceptable performance. */
        val Medium = Color(0xFFF59E0B) // Amber

        /** Slow/Poor performance. */
        val Slow = Color(0xFFEF4444) // Red

        /** 2xx Success responses. */
        val Success2xx = Color(0xFF10B981)

        /** 3xx Redirect responses. */
        val Redirect3xx = Color(0xFF3B82F6)

        /** 4xx Client error responses. */
        val ClientError4xx = Color(0xFFF59E0B)

        /** 5xx Server error responses. */
        val ServerError5xx = Color(0xFFEF4444)
    }

    // ============================================================
    // CATEGORY COLORS - For tool groupings
    // ============================================================

    /** Colors for tool category groupings in the tools grid. */
    object Category {
        /** Inspection tools - Indigo (analytical, detailed). */
        val Inspection = Color(0xFF6366F1)

        /** Performance tools - Amber (speed, optimization). */
        val Performance = Color(0xFFF59E0B)

        /** Network tools - Emerald (data flow, connectivity). */
        val Network = Color(0xFF10B981)

        /** Simulation tools - Purple (testing, mocking). */
        val Simulation = Color(0xFF8B5CF6)

        /** Core tools - Blue (essential, foundational). */
        val Core = Color(0xFF3B82F6)

        /** Favorites - Amber (highlighted, important). */
        val Favorites = Color(0xFFF59E0B)

        /** Fallback/default category. */
        val Fallback = Color(0xFF6B7280)
    }

    // ============================================================
    // CONTENT TYPE COLORS - For data format indicators
    // ============================================================

    /** Colors for distinguishing HTTP content types (JSON, XML, HTML, etc.). */
    object ContentType {
        /** JSON data. */
        val Json = Color(0xFFF59E0B) // Amber

        /** XML data. */
        val Xml = Color(0xFF8B5CF6) // Purple

        /** HTML content. */
        val Html = Color(0xFFEC4899) // Pink

        /** Protocol Buffers. */
        val Protobuf = Color(0xFF10B981) // Emerald

        /** Form data. */
        val FormData = Color(0xFF3B82F6) // Blue

        /** Multipart form data. */
        val Multipart = Color(0xFF6366F1) // Indigo

        /** Plain text. */
        val PlainText = Color(0xFF6B7280) // Gray

        /** Binary data. */
        val Binary = Color(0xFFEF4444) // Red

        /** PDF documents. */
        val Pdf = Color(0xFFDC2626) // Red-600

        /** Image content. */
        val Image = Color(0xFF14B8A6) // Teal

        /** Unknown content type. */
        val Unknown = Color(0xFF9CA3AF) // Gray-400
    }

    // ============================================================
    // HTTP METHOD COLORS - For request method indicators
    // ============================================================

    /** Colors for HTTP method indicators (GET, POST, PUT, etc.). */
    object HttpMethod {
        /** GET request - retrieval. */
        val Get = Color(0xFF3B82F6) // Blue

        /** POST request - creation. */
        val Post = Color(0xFF10B981) // Green

        /** PUT request - replacement. */
        val Put = Color(0xFFF59E0B) // Amber

        /** PATCH request - modification. */
        val Patch = Color(0xFF8B5CF6) // Purple

        /** DELETE request - removal. */
        val Delete = Color(0xFFEF4444) // Red

        /** HEAD request - metadata. */
        val Head = Color(0xFF6B7280) // Gray

        /** OPTIONS request - capabilities. */
        val Options = Color(0xFF6366F1) // Indigo

        /** CONNECT request - tunnel. */
        val Connect = Color(0xFF14B8A6) // Teal

        /** TRACE request - debugging. */
        val Trace = Color(0xFF9CA3AF) // Gray-400

        /** Returns the color for the given HTTP method name. */
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

    /** Accent colors for highlights and emphasis elements. */
    object Accent {
        /** Tertiary accent - teal. */
        val Tertiary = Color(0xFF14B8A6)

        /** Highlight color for search results. */
        val Highlight = Color(0xFFFBBF24) // Yellow-400
    }

    // ============================================================
    // FEATURE-SPECIFIC COLORS
    // Consolidated from individual feature design systems
    // ============================================================

    /**
     * Memory monitoring feature colors.
     */
    object Memory {
        /** Used heap memory indicator. */
        val HeapUsed = Color(0xFF4CAF50) // Green

        /** Free heap memory indicator. */
        val HeapFree = Color(0xFFC8E6C9) // Light Green

        /** Total heap memory indicator. */
        val HeapTotal = Color(0xFF81C784) // Medium Green

        /** Native heap memory indicator. */
        val NativeHeap = Color(0xFF2196F3) // Blue
    }

    /**
     * CPU monitoring feature colors.
     */
    object Cpu {
        /** Primary CPU usage indicator color. */
        val Usage = Color(0xFF2196F3) // Blue

        /** Light CPU usage background color. */
        val UsageLight = Color(0xFFBBDEFB) // Light Blue

        /** Per-core colors for multi-core visualization. */
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

        /** Returns a color for a specific CPU core index, cycling through available colors. */
        fun forCore(index: Int): Color = CoreColors[index % CoreColors.size]
    }

    /**
     * WebSocket feature colors.
     */
    object WebSocket {
        /** Connection state: connecting in progress. */
        val Connecting = Color(0xFFF57C00) // Orange

        /** Connection state: open and active. */
        val Open = Color(0xFF388E3C) // Green

        /** Connection state: closing in progress. */
        val Closing = Color(0xFFE65100) // Deep Orange

        /** Connection state: closed. */
        val Closed = Color(0xFF757575) // Gray

        /** Message direction: sent by client. */
        val Sent = Color(0xFF1976D2) // Blue

        /** Message direction: received from server. */
        val Received = Color(0xFF388E3C) // Green

        /** Message type: text payload. */
        val TextMessage = Color(0xFF424242) // Gray

        /** Message type: binary payload. */
        val BinaryMessage = Color(0xFF7B1FA2) // Purple

        /** Message type: ping/pong control frame. */
        val PingPong = Color(0xFF0097A7) // Cyan
    }

    /**
     * Database feature colors.
     */
    object Database {
        /** Integer data type indicator. */
        val Integer = Color(0xFF4FC3F7) // Light Blue

        /** Real/float data type indicator. */
        val Real = Color(0xFF81C784) // Light Green

        /** Text data type indicator. */
        val Text = Color(0xFFFFB74D) // Orange

        /** Blob data type indicator. */
        val Blob = Color(0xFFBA68C8) // Purple

        /** Null value indicator. */
        val NullValue = Color(0xFF90A4AE) // Blue Grey

        /** Primary key column indicator. */
        val PrimaryKey = Color(0xFFFFD54F) // Amber

        /** SQL keyword syntax highlight color. */
        val SqlKeyword = Color(0xFF569CD6) // Blue

        /** SQL string literal syntax highlight color. */
        val SqlString = Color(0xFFCE9178) // Orange

        /** SQL number literal syntax highlight color. */
        val SqlNumber = Color(0xFFB5CEA8) // Green

        /** SQL function name syntax highlight color. */
        val SqlFunction = Color(0xFFDCDCAA) // Yellow

        /** SQL operator syntax highlight color. */
        val SqlOperator = Color(0xFFD4D4D4) // Light Grey

        /** SQL comment syntax highlight color. */
        val SqlComment = Color(0xFF6A9955) // Green

        /** SQL table name syntax highlight color. */
        val SqlTable = Color(0xFF4EC9B0) // Cyan

        /** Returns a color for a given SQL data type name. */
        fun forDataType(type: String): Color = when (type.uppercase()) {
            "INTEGER", "INT", "BIGINT", "SMALLINT", "TINYINT" -> Integer
            "REAL", "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC" -> Real
            "TEXT", "VARCHAR", "CHAR", "CLOB" -> Text
            "BLOB", "BINARY", "VARBINARY" -> Blob
            else -> Text
        }
    }

    /**
     * Location simulation feature colors.
     */
    object Location {
        /** Mock location enabled/active state. */
        val Enabled = Color(0xFF4CAF50) // Green - mock active

        /** Mock location disabled/inactive state. */
        val Disabled = Color(0xFF9E9E9E) // Gray - mock inactive

        /** Location warning state. */
        val Warning = Color(0xFFFF9800) // Orange

        /** Location error state. */
        val Error = Color(0xFFF44336) // Red

        /** Built-in location preset indicator. */
        val BuiltInPreset = Color(0xFF2196F3) // Blue

        /** User-defined location preset indicator. */
        val UserPreset = Color(0xFF9C27B0) // Purple

        /** Coordinate display color. */
        val Coordinate = Color(0xFF00BCD4) // Cyan
    }

    /**
     * Leak detection feature colors.
     */
    object LeakDetection {
        /** Critical severity leak indicator. */
        val Critical = Color(0xFFD32F2F) // Red 700

        /** High severity leak indicator. */
        val High = Color(0xFFE65100) // Orange 800

        /** Medium severity leak indicator. */
        val Medium = Color(0xFFF9A825) // Yellow 800

        /** Low severity leak indicator. */
        val Low = Color(0xFF00838F) // Cyan 800

        /** Active monitoring state indicator. */
        val Monitoring = Color(0xFF4CAF50) // Green

        /** Idle/inactive state indicator. */
        val Idle = Color(0xFF9E9E9E) // Grey
    }

    /**
     * Thread violation feature colors.
     */
    object ThreadViolation {
        /** Disk read violation indicator. */
        val DiskRead = Color(0xFF2196F3) // Blue

        /** Disk write violation indicator. */
        val DiskWrite = Color(0xFF1565C0) // Blue 800

        /** Network violation on main thread indicator. */
        val Network = Color(0xFFFF9800) // Orange

        /** Slow call violation indicator. */
        val SlowCall = Color(0xFFF44336) // Red

        /** Custom slow code violation indicator. */
        val CustomSlowCode = Color(0xFF9C27B0) // Purple

        /** Active monitoring state indicator. */
        val Monitoring = Color(0xFF4CAF50) // Green

        /** Idle/inactive state indicator. */
        val Idle = Color(0xFF9E9E9E) // Grey
    }

    /**
     * Secure storage feature colors.
     */
    object SecureStorage {
        /** Primary secure storage accent color. */
        val Primary = Color(0xFF5C6BC0) // Indigo

        /** Encrypted data indicator. */
        val Encrypted = Color(0xFF4CAF50) // Green

        /** Unencrypted data warning indicator. */
        val Unencrypted = Color(0xFFFF9800) // Orange

        /** EncryptedSharedPreferences storage type indicator. */
        val EncryptedPrefs = Color(0xFF7C4DFF) // Deep Purple

        /** Android Keystore storage type indicator. */
        val Keystore = Color(0xFF2196F3) // Blue

        /** Jetpack DataStore storage type indicator. */
        val Datastore = Color(0xFF009688) // Teal
    }
}
