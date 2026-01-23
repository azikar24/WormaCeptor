/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a detected library/dependency in the application.
 *
 * @param name Display name of the library (e.g., "OkHttp")
 * @param groupId Maven group ID if known (e.g., "com.squareup.okhttp3")
 * @param artifactId Maven artifact ID if known (e.g., "okhttp")
 * @param version Detected version string, or null if unknown
 * @param category Functional category of the library
 * @param detectionMethod How the library/version was detected
 * @param packageName The main package used for detection
 * @param isDetected Whether the library was found in the classpath
 * @param description Brief description of what the library does
 * @param website Official website or repository URL
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
    NETWORKING,
    DEPENDENCY_INJECTION,
    UI_FRAMEWORK,
    IMAGE_LOADING,
    SERIALIZATION,
    DATABASE,
    REACTIVE,
    LOGGING,
    ANALYTICS,
    TESTING,
    SECURITY,
    UTILITY,
    ANDROIDX,
    KOTLIN,
    OTHER,
    ;

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

    fun displayName(): String = when (this) {
        VERSION_FIELD -> "VERSION field"
        BUILD_CONFIG -> "BuildConfig"
        USER_AGENT -> "User-Agent"
        PACKAGE_ANNOTATION -> "Package annotation"
        MANIFEST_METADATA -> "Manifest metadata"
        CLASS_PRESENCE_ONLY -> "Class detection"
        JAR_MANIFEST -> "JAR manifest"
    }

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
    val totalDetected: Int,
    val withVersion: Int,
    val withoutVersion: Int,
    val byCategory: Map<DependencyCategory, Int>,
) {
    companion object {
        fun empty() = DependencySummary(
            totalDetected = 0,
            withVersion = 0,
            withoutVersion = 0,
            byCategory = emptyMap(),
        )
    }
}
