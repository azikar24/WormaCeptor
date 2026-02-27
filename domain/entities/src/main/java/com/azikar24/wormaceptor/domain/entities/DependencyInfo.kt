package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a detected library/dependency in the application.
 *
 * @property name Display name of the library (e.g., "OkHttp").
 * @property groupId Maven group ID if known (e.g., "com.squareup.okhttp3").
 * @property artifactId Maven artifact ID if known (e.g., "okhttp").
 * @property version Detected version string, or null if unknown.
 * @property category Functional category of the library.
 * @property detectionMethod How the library/version was detected.
 * @property packageName The main package used for detection.
 * @property isDetected Whether the library was found in the classpath.
 * @property description Brief description of what the library does.
 * @property website Official website or repository URL.
 * @property isInternalDependency Whether this is an internal (first-party) dependency
 *   rather than a third-party library.
 */
data class DependencyInfo(
    val name: String,
    val groupId: String?,
    val artifactId: String?,
    val version: String?,
    val category: DependencyCategory,
    val detectionMethod: DetectionMethod,
    val packageName: String,
    val isDetected: Boolean,
    val description: String,
    val website: String?,
    val isInternalDependency: Boolean = false,
) {
    /**
     * Returns the Maven coordinate string if available.
     */
    val mavenCoordinate: String?
        get() = if (groupId != null && artifactId != null) {
            if (version != null) "$groupId:$artifactId:$version" else "$groupId:$artifactId"
        } else {
            null
        }
}

/**
 * Categories for organizing dependencies by their primary function.
 */
enum class DependencyCategory {
    /** HTTP clients, WebSocket, and REST libraries. */
    NETWORKING,

    /** DI frameworks like Koin, Dagger, or Hilt. */
    DEPENDENCY_INJECTION,

    /** Compose, View-based UI, and widget libraries. */
    UI_FRAMEWORK,

    /** Image loading and caching (Glide, Coil, Picasso). */
    IMAGE_LOADING,

    /** JSON, XML, and protobuf serialization libraries. */
    SERIALIZATION,

    /** Room, SQLDelight, Realm, and other persistence libraries. */
    DATABASE,

    /** RxJava, Kotlin Flow extensions, and reactive stream libraries. */
    REACTIVE,

    /** Logging frameworks (Timber, SLF4J). */
    LOGGING,

    /** Analytics and crash reporting SDKs. */
    ANALYTICS,

    /** Unit test, UI test, and mocking frameworks. */
    TESTING,

    /** Encryption, certificate pinning, and security libraries. */
    SECURITY,

    /** General-purpose utility libraries. */
    UTILITY,

    /** AndroidX Jetpack libraries. */
    ANDROIDX,

    /** Kotlin standard library and official extensions. */
    KOTLIN,

    /** Libraries that do not fit any other category. */
    OTHER,
    ;

    /** Returns the human-readable label for this category. */
    fun displayName(): String = when (this) {
        NETWORKING -> "Networking"
        DEPENDENCY_INJECTION -> "Dependency Injection"
        UI_FRAMEWORK -> "UI Framework"
        IMAGE_LOADING -> "Image Loading"
        SERIALIZATION -> "Serialization"
        DATABASE -> "Database"
        REACTIVE -> "Reactive"
        LOGGING -> "Logging"
        ANALYTICS -> "Analytics"
        TESTING -> "Testing"
        SECURITY -> "Security"
        UTILITY -> "Utility"
        ANDROIDX -> "AndroidX"
        KOTLIN -> "Kotlin"
        OTHER -> "Other"
    }
}

/**
 * How the library or its version was detected.
 */
enum class DetectionMethod {
    /** Version found via static VERSION field */
    VERSION_FIELD,

    /** Version found via BuildConfig class */
    BUILD_CONFIG,

    /** Version found via user-agent or other runtime string */
    USER_AGENT,

    /** Version found via package annotation */
    PACKAGE_ANNOTATION,

    /** Version found via manifest metadata */
    MANIFEST_METADATA,

    /** Library detected but version could not be determined */
    CLASS_PRESENCE_ONLY,

    /** Version extracted from JAR manifest */
    JAR_MANIFEST,
    ;

    /** Returns the human-readable label for this detection method. */
    fun displayName(): String = when (this) {
        VERSION_FIELD -> "VERSION field"
        BUILD_CONFIG -> "BuildConfig"
        USER_AGENT -> "User-Agent"
        PACKAGE_ANNOTATION -> "Package annotation"
        MANIFEST_METADATA -> "Manifest metadata"
        CLASS_PRESENCE_ONLY -> "Class detection"
        JAR_MANIFEST -> "JAR manifest"
    }

    /** Returns a confidence rating (High, Medium, Low) for this detection method. */
    fun confidence(): String = when (this) {
        VERSION_FIELD -> "High"
        BUILD_CONFIG -> "High"
        USER_AGENT -> "High"
        PACKAGE_ANNOTATION -> "Medium"
        MANIFEST_METADATA -> "Medium"
        CLASS_PRESENCE_ONLY -> "Low"
        JAR_MANIFEST -> "High"
    }
}

/**
 * Summary statistics for detected dependencies.
 */
data class DependencySummary(
    /** Total number of libraries detected in the classpath. */
    val totalDetected: Int,
    /** Number of libraries where the version string was resolved. */
    val withVersion: Int,
    /** Number of libraries detected without a version string. */
    val withoutVersion: Int,
    /** Breakdown of detected libraries grouped by functional category. */
    val byCategory: Map<DependencyCategory, Int>,
) {
    /** Factory methods for [DependencySummary]. */
    companion object {
        /** Creates an empty summary with zero counts. */
        fun empty() = DependencySummary(
            totalDetected = 0,
            withVersion = 0,
            withoutVersion = 0,
            byCategory = emptyMap(),
        )
    }
}
