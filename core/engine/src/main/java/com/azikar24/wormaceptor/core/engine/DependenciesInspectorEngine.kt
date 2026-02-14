package com.azikar24.wormaceptor.core.engine

import android.content.Context
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.domain.entities.DetectionMethod
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Enumeration

/**
 * Engine that detects Java/Kotlin libraries present in the application.
 *
 * Uses multiple detection strategies:
 * - Class presence checks via reflection
 * - Static VERSION field extraction
 * - BuildConfig class inspection
 * - User-Agent string parsing (for networking libraries)
 * - Package annotations
 *
 * @param context Application context
 */
@Suppress("TooManyFunctions")
class DependenciesInspectorEngine(
    @Suppress("unused") private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Detected dependencies
    private val _dependencies = MutableStateFlow<List<DependencyInfo>>(emptyList())
    val dependencies: StateFlow<List<DependencyInfo>> = _dependencies.asStateFlow()

    // Summary statistics
    private val _summary = MutableStateFlow(DependencySummary.empty())
    val summary: StateFlow<DependencySummary> = _summary.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    /**
     * Refreshes the dependency scan.
     */
    fun refresh() {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val detected = withContext(Dispatchers.IO) {
                    scanDependencies()
                }
                _dependencies.value = detected
                _summary.value = calculateSummary(detected)
            } catch (e: Exception) {
                _error.value = "Failed to scan dependencies: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Scans for all libraries - both known and dynamically discovered.
     * Excludes WormaCeptor's internal dependencies.
     */
    private fun scanDependencies(): List<DependencyInfo> {
        val results = mutableMapOf<String, DependencyInfo>()

        // Step 1: Detect known libraries with rich metadata (skip internal ones)
        KNOWN_LIBRARIES.forEach { spec ->
            // Skip WormaCeptor internal libraries
            if (isWormaCeptorInternal(spec.name, spec.packageName)) return@forEach

            val detected = detectLibrary(spec)
            if (detected.isDetected && !detected.isInternalDependency) {
                results[spec.packageName] = detected
            }
        }

        // Step 2: Dynamically scan for unknown libraries (skip internal ones)
        val dynamicLibraries = scanDynamicDependencies()
        dynamicLibraries.forEach { lib ->
            // Skip internal and already covered libraries
            if (lib.isInternalDependency) return@forEach
            if (results.containsKey(lib.packageName)) return@forEach
            if (isKnownPackageCovered(lib.packageName, results.keys)) return@forEach

            results[lib.packageName] = lib
        }

        return results.values
            .sortedWith(compareBy({ it.category.ordinal }, { it.name }))
    }

    /**
     * Checks if a library is used internally by WormaCeptor.
     */
    private fun isWormaCeptorInternal(name: String, packageName: String): Boolean {
        return name in WORMACEPTOR_INTERNAL_LIBRARIES ||
            WORMACEPTOR_INTERNAL_PACKAGES.any { packageName.startsWith(it) }
    }

    /**
     * Checks if a package is already covered by a known library.
     */
    private fun isKnownPackageCovered(packageName: String, knownPackages: Set<String>): Boolean {
        return knownPackages.any { known ->
            packageName.startsWith("$known.") || packageName == known
        }
    }

    /**
     * Dynamically scans loaded classes to discover third-party libraries.
     */
    private fun scanDynamicDependencies(): List<DependencyInfo> {
        val discoveredPackages = mutableSetOf<String>()

        try {
            // Get all loaded class names from the ClassLoader
            val classLoader = context.classLoader
            if (classLoader is BaseDexClassLoader) {
                val pathListField = BaseDexClassLoader::class.java.getDeclaredField("pathList")
                pathListField.isAccessible = true
                val pathList = pathListField.get(classLoader)

                val dexElementsField = pathList.javaClass.getDeclaredField("dexElements")
                dexElementsField.isAccessible = true
                val dexElements = dexElementsField.get(pathList) as Array<*>

                for (element in dexElements) {
                    try {
                        val dexFileField = element?.javaClass?.getDeclaredField("dexFile")
                        dexFileField?.isAccessible = true
                        val dexFile = dexFileField?.get(element) as? DexFile ?: continue

                        val entries: Enumeration<String> = dexFile.entries()
                        while (entries.hasMoreElements()) {
                            val className = entries.nextElement()
                            // Extract package name (up to 3 levels deep)
                            val packageName = extractLibraryPackage(className)
                            if (packageName != null && shouldIncludePackage(packageName)) {
                                discoveredPackages.add(packageName)
                            }
                        }
                    } catch (e: Exception) {
                        // Skip this element
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback: scanning failed, return empty
        }

        // Convert discovered packages to DependencyInfo
        return discoveredPackages
            .filter { !isSystemPackage(it) && !isAppPackage(it) }
            .mapNotNull { packageName -> createDynamicDependency(packageName) }
    }

    /**
     * Extracts a meaningful library package from a full class name.
     * e.g., "com.squareup.okhttp3.internal.connection.Exchange" -> "com.squareup.okhttp3"
     */
    private fun extractLibraryPackage(className: String): String? {
        val parts = className.split(".")
        if (parts.size < 3) return null

        // For most libraries, 3 levels is enough: com.company.library
        // Some use 2 levels: okhttp3, retrofit2
        return when {
            // Handle common patterns
            parts[0] == "com" || parts[0] == "org" || parts[0] == "io" || parts[0] == "net" -> {
                parts.take(3).joinToString(".")
            }
            // Handle short packages like okhttp3, retrofit2
            parts[0].matches(Regex("^[a-z]+\\d*$")) && parts.size >= 2 -> {
                parts.take(2).joinToString(".")
            }
            else -> parts.take(3).joinToString(".")
        }
    }

    /**
     * Determines if a package should be included in the scan.
     */
    private fun shouldIncludePackage(packageName: String): Boolean {
        // Exclude common non-library packages
        val excludePrefixes = listOf(
            "java.", "javax.", "sun.", "dalvik.", "libcore.",
            "android.", "androidx.annotation", "kotlin.jvm.internal",
            "kotlinx.coroutines.internal",
        )
        return excludePrefixes.none { packageName.startsWith(it) }
    }

    /**
     * Checks if a package is a system/framework package.
     */
    private fun isSystemPackage(packageName: String): Boolean {
        val systemPrefixes = listOf(
            "java.", "javax.", "sun.", "dalvik.", "libcore.",
            "android.support.", "com.android.internal.",
            "kotlin.jvm.", "kotlin.reflect.", "kotlin.collections.",
            "kotlin.sequences.", "kotlin.text.", "kotlin.io.",
            "kotlin.ranges.", "kotlin.annotation.", "kotlin.comparisons.",
        )
        return systemPrefixes.any { packageName.startsWith(it) }
    }

    /**
     * Checks if a package belongs to the host app.
     */
    private fun isAppPackage(packageName: String): Boolean {
        val appPackage = context.packageName
        return packageName.startsWith(appPackage) ||
            packageName.startsWith("com.azikar24.wormaceptor")
    }

    /**
     * Creates a DependencyInfo from a dynamically discovered package.
     */
    private fun createDynamicDependency(packageName: String): DependencyInfo? {
        // Try to categorize based on package patterns
        val (name, category) = categorizePackage(packageName)

        // Check if this is a WormaCeptor internal dependency
        val isInternal = WORMACEPTOR_INTERNAL_PACKAGES.any { internalPkg ->
            packageName.startsWith(internalPkg)
        }

        // Try to find version
        val version = tryFindVersionDynamically(packageName)

        return DependencyInfo(
            name = name,
            groupId = inferGroupId(packageName),
            artifactId = inferArtifactId(packageName),
            version = version?.first,
            category = category,
            detectionMethod = version?.second ?: DetectionMethod.CLASS_PRESENCE_ONLY,
            packageName = packageName,
            isDetected = true,
            description = "Dynamically discovered library",
            website = null,
            isInternalDependency = isInternal,
        )
    }

    /**
     * Categorizes a package and generates a display name.
     */
    private fun categorizePackage(packageName: String): Pair<String, DependencyCategory> {
        // Try to create a nice display name
        val parts = packageName.split(".")
        val displayName = when {
            parts.size >= 3 -> parts[2].replaceFirstChar { it.uppercase() }
            parts.size >= 2 -> parts[1].replaceFirstChar { it.uppercase() }
            else -> parts.last().replaceFirstChar { it.uppercase() }
        }

        // Categorize based on package patterns
        val category = when {
            packageName.contains("http") || packageName.contains("network") ||
                packageName.contains("retrofit") || packageName.contains("okhttp") ||
                packageName.contains("ktor") || packageName.contains("volley") ||
                packageName.contains("grpc") || packageName.contains("apollo") -> DependencyCategory.NETWORKING

            packageName.contains("inject") || packageName.contains("dagger") ||
                packageName.contains("hilt") || packageName.contains("koin") ||
                packageName.contains("kodein") -> DependencyCategory.DEPENDENCY_INJECTION

            packageName.contains("glide") || packageName.contains("picasso") ||
                packageName.contains("coil") || packageName.contains("fresco") ||
                packageName.contains("image") -> DependencyCategory.IMAGE_LOADING

            packageName.contains("gson") || packageName.contains("moshi") ||
                packageName.contains("jackson") || packageName.contains("serialization") ||
                packageName.contains("protobuf") || packageName.contains("json") -> DependencyCategory.SERIALIZATION

            packageName.contains("room") || packageName.contains("realm") ||
                packageName.contains("sqlite") || packageName.contains("database") ||
                packageName.contains("sqldelight") || packageName.contains("objectbox") -> DependencyCategory.DATABASE

            packageName.contains("rxjava") || packageName.contains("reactivex") ||
                packageName.contains("coroutine") || packageName.contains("flow") -> DependencyCategory.REACTIVE

            packageName.contains("timber") || packageName.contains("log") ||
                packageName.contains("slf4j") -> DependencyCategory.LOGGING

            packageName.contains("firebase") || packageName.contains("analytics") ||
                packageName.contains("crashlytics") -> DependencyCategory.ANALYTICS

            packageName.contains("compose") || packageName.contains("material") ||
                packageName.contains("widget") || packageName.contains("view") -> DependencyCategory.UI_FRAMEWORK

            packageName.contains("security") || packageName.contains("crypto") ||
                packageName.contains("cipher") || packageName.contains("tink") -> DependencyCategory.SECURITY

            packageName.contains("test") || packageName.contains("mock") ||
                packageName.contains("espresso") || packageName.contains("junit") -> DependencyCategory.TESTING

            packageName.startsWith("androidx.") -> DependencyCategory.ANDROIDX
            packageName.startsWith("kotlin") -> DependencyCategory.KOTLIN

            else -> DependencyCategory.OTHER
        }

        return displayName to category
    }

    /**
     * Tries to find version information for a dynamically discovered package.
     */
    private fun tryFindVersionDynamically(packageName: String): Pair<String, DetectionMethod>? {
        // Try common version patterns
        val versionPatterns = listOf(
            "$packageName.BuildConfig" to "VERSION",
            "$packageName.BuildConfig" to "VERSION_NAME",
            "$packageName.Version" to "VERSION",
            "$packageName.internal.Version" to "userAgent",
        )

        for ((className, fieldName) in versionPatterns) {
            tryGetStaticField(className, fieldName)?.let { version ->
                val cleanVersion = if (fieldName == "userAgent") {
                    extractVersionFromUserAgent(version)
                } else {
                    version
                }
                cleanVersion?.let {
                    return it to if (fieldName == "userAgent") {
                        DetectionMethod.USER_AGENT
                    } else {
                        DetectionMethod.VERSION_FIELD
                    }
                }
            }
        }

        return null
    }

    /**
     * Infers Maven groupId from package name.
     */
    private fun inferGroupId(packageName: String): String {
        val parts = packageName.split(".")
        return if (parts.size >= 2) parts.take(2).joinToString(".") else packageName
    }

    /**
     * Infers Maven artifactId from package name.
     */
    private fun inferArtifactId(packageName: String): String {
        val parts = packageName.split(".")
        return when {
            parts.size >= 3 -> parts[2]
            parts.size >= 2 -> parts[1]
            else -> parts.last()
        }
    }

    /**
     * Detects a single library and extracts its version if possible.
     */
    private fun detectLibrary(spec: LibrarySpec): DependencyInfo {
        // Check if the main detection class exists
        val isPresent = isClassPresent(spec.detectionClass)

        if (!isPresent) {
            return spec.toUndetected()
        }

        // Try to extract version using various methods
        val (version, method) = extractVersion(spec)

        // Check if this is a WormaCeptor internal dependency
        val isInternal = WORMACEPTOR_INTERNAL_PACKAGES.any { internalPkg ->
            spec.packageName.startsWith(internalPkg) || spec.groupId?.contains("wormaceptor") == true
        } || spec.name in WORMACEPTOR_INTERNAL_LIBRARIES

        return DependencyInfo(
            name = spec.name,
            groupId = spec.groupId,
            artifactId = spec.artifactId,
            version = version,
            category = spec.category,
            detectionMethod = method,
            packageName = spec.packageName,
            isDetected = true,
            description = spec.description,
            website = spec.website,
            isInternalDependency = isInternal,
        )
    }

    /**
     * Extracts version using multiple strategies.
     */
    private fun extractVersion(spec: LibrarySpec): Pair<String?, DetectionMethod> {
        // Strategy 1: Try VERSION field
        spec.versionClass?.let { versionClass ->
            spec.versionField?.let { versionField ->
                tryGetStaticField(versionClass, versionField)?.let { version ->
                    return version to DetectionMethod.VERSION_FIELD
                }
            }
        }

        // Strategy 2: Try BuildConfig
        spec.buildConfigClass?.let { buildConfigClass ->
            tryGetStaticField(buildConfigClass, "VERSION")?.let { version ->
                return version to DetectionMethod.BUILD_CONFIG
            }
            tryGetStaticField(buildConfigClass, "VERSION_NAME")?.let { version ->
                return version to DetectionMethod.BUILD_CONFIG
            }
        }

        // Strategy 3: Try custom version extraction
        spec.customVersionExtractor?.let { extractor ->
            extractor()?.let { (version, method) ->
                return version to method
            }
        }

        // Strategy 4: Check if library has any common version patterns
        tryCommonVersionPatterns(spec)?.let { (version, method) ->
            return version to method
        }

        return null to DetectionMethod.CLASS_PRESENCE_ONLY
    }

    /**
     * Tries common version field patterns that libraries often use.
     */
    private fun tryCommonVersionPatterns(spec: LibrarySpec): Pair<String, DetectionMethod>? {
        val packageBase = spec.packageName

        // Common version class/field patterns
        val patterns = listOf(
            "$packageBase.BuildConfig" to "VERSION",
            "$packageBase.BuildConfig" to "VERSION_NAME",
            "$packageBase.Version" to "VERSION",
            "$packageBase.Version" to "version",
            "$packageBase.internal.Version" to "userAgent",
        )

        for ((className, fieldName) in patterns) {
            tryGetStaticField(className, fieldName)?.let { version ->
                // Parse version from user-agent if needed
                val cleanVersion = if (fieldName == "userAgent") {
                    extractVersionFromUserAgent(version)
                } else {
                    version
                }
                cleanVersion?.let {
                    return it to if (fieldName == "userAgent") {
                        DetectionMethod.USER_AGENT
                    } else {
                        DetectionMethod.VERSION_FIELD
                    }
                }
            }
        }

        return null
    }

    /**
     * Extracts version number from a user-agent string.
     * Example: "okhttp/4.12.0" -> "4.12.0"
     */
    private fun extractVersionFromUserAgent(userAgent: String): String? {
        val versionPattern = Regex("""[\d]+\.[\d]+(?:\.[\d]+)?(?:-[\w]+)?""")
        return versionPattern.find(userAgent)?.value
    }

    /**
     * Checks if a class exists in the classpath.
     */
    private fun isClassPresent(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: NoClassDefFoundError) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Tries to get a static field value from a class.
     */
    private fun tryGetStaticField(className: String, fieldName: String): String? {
        return try {
            val clazz = Class.forName(className)
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            val value = field.get(null)
            value?.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateSummary(dependencies: List<DependencyInfo>): DependencySummary {
        val detected = dependencies.filter { it.isDetected }
        val withVersion = detected.count { it.version != null }
        val byCategory = detected.groupBy { it.category }
            .mapValues { it.value.size }

        return DependencySummary(
            totalDetected = detected.size,
            withVersion = withVersion,
            withoutVersion = detected.size - withVersion,
            byCategory = byCategory,
        )
    }

    /**
     * Exports dependency information as text.
     */
    fun exportAsText(): String {
        val sb = StringBuilder()
        val summary = _summary.value
        val deps = _dependencies.value

        sb.appendLine("=== Dependencies Report ===")
        sb.appendLine(
            "Generated: ${
                java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.US,
                ).format(java.util.Date())
            }",
        )
        sb.appendLine()

        sb.appendLine("=== Summary ===")
        sb.appendLine("Total Detected: ${summary.totalDetected}")
        sb.appendLine("With Version: ${summary.withVersion}")
        sb.appendLine("Without Version: ${summary.withoutVersion}")
        sb.appendLine()

        sb.appendLine("=== By Category ===")
        summary.byCategory.entries.sortedByDescending { it.value }.forEach { (category, count) ->
            sb.appendLine("${category.displayName()}: $count")
        }
        sb.appendLine()

        sb.appendLine("=== Detected Libraries ===")
        deps.groupBy { it.category }.forEach { (category, libraries) ->
            sb.appendLine()
            sb.appendLine("--- ${category.displayName()} ---")
            libraries.forEach { lib ->
                sb.appendLine("${lib.name}")
                lib.version?.let { sb.appendLine("  Version: $it") }
                lib.mavenCoordinate?.let { sb.appendLine("  Maven: $it") }
                sb.appendLine("  Detection: ${lib.detectionMethod.displayName()}")
                sb.appendLine("  Package: ${lib.packageName}")
            }
        }

        return sb.toString()
    }

    companion object {
        /**
         * Only WormaCeptor's own packages are excluded.
         * Third-party libraries (even if used by WormaCeptor) are shown
         * because the host app might also use them.
         */
        private val WORMACEPTOR_INTERNAL_PACKAGES = setOf(
            "com.azikar24.wormaceptor",
        )

        /**
         * No libraries are excluded - if the host app uses Koin, Coil, etc.
         * they should be shown even if WormaCeptor also uses them.
         */
        private val WORMACEPTOR_INTERNAL_LIBRARIES = emptySet<String>()

        /**
         * Registry of known libraries with their detection strategies.
         */
        private val KNOWN_LIBRARIES = listOf(
            // ============ NETWORKING ============
            LibrarySpec(
                name = "OkHttp",
                groupId = "com.squareup.okhttp3",
                artifactId = "okhttp",
                category = DependencyCategory.NETWORKING,
                packageName = "okhttp3",
                detectionClass = "okhttp3.OkHttpClient",
                description = "HTTP client for Android and Java",
                website = "https://square.github.io/okhttp/",
                customVersionExtractor = {
                    // OkHttp stores version in okhttp3.internal.Version
                    try {
                        val clazz = Class.forName("okhttp3.internal.Version")
                        val method = clazz.getDeclaredMethod("userAgent")
                        method.isAccessible = true
                        val userAgent = method.invoke(null)?.toString()
                        userAgent?.let {
                            val version = Regex("""okhttp/([\d.]+)""").find(it)?.groupValues?.get(1)
                            version?.let { v -> v to DetectionMethod.USER_AGENT }
                        }
                    } catch (e: Exception) {
                        null
                    }
                },
            ),
            LibrarySpec(
                name = "Retrofit",
                groupId = "com.squareup.retrofit2",
                artifactId = "retrofit",
                category = DependencyCategory.NETWORKING,
                packageName = "retrofit2",
                detectionClass = "retrofit2.Retrofit",
                description = "Type-safe HTTP client for Android and Java",
                website = "https://square.github.io/retrofit/",
            ),
            LibrarySpec(
                name = "Ktor Client",
                groupId = "io.ktor",
                artifactId = "ktor-client-core",
                category = DependencyCategory.NETWORKING,
                packageName = "io.ktor.client",
                detectionClass = "io.ktor.client.HttpClient",
                description = "Asynchronous HTTP client for Kotlin",
                website = "https://ktor.io/",
            ),
            LibrarySpec(
                name = "Volley",
                groupId = "com.android.volley",
                artifactId = "volley",
                category = DependencyCategory.NETWORKING,
                packageName = "com.android.volley",
                detectionClass = "com.android.volley.RequestQueue",
                description = "HTTP library by Google",
                website = "https://google.github.io/volley/",
            ),
            LibrarySpec(
                name = "Apollo GraphQL",
                groupId = "com.apollographql.apollo3",
                artifactId = "apollo-runtime",
                category = DependencyCategory.NETWORKING,
                packageName = "com.apollographql.apollo3",
                detectionClass = "com.apollographql.apollo3.ApolloClient",
                description = "GraphQL client for Kotlin",
                website = "https://www.apollographql.com/",
            ),

            // ============ DEPENDENCY INJECTION ============
            LibrarySpec(
                name = "Koin",
                groupId = "io.insert-koin",
                artifactId = "koin-core",
                category = DependencyCategory.DEPENDENCY_INJECTION,
                packageName = "org.koin.core",
                detectionClass = "org.koin.core.Koin",
                description = "Pragmatic lightweight DI framework for Kotlin",
                website = "https://insert-koin.io/",
                customVersionExtractor = {
                    try {
                        val clazz = Class.forName("org.koin.core.context.KoinContext")
                        // Koin 3.x stores version differently
                        null
                    } catch (e: Exception) {
                        null
                    }
                },
            ),
            LibrarySpec(
                name = "Dagger",
                groupId = "com.google.dagger",
                artifactId = "dagger",
                category = DependencyCategory.DEPENDENCY_INJECTION,
                packageName = "dagger",
                detectionClass = "dagger.Component",
                description = "Compile-time DI framework",
                website = "https://dagger.dev/",
            ),
            LibrarySpec(
                name = "Hilt",
                groupId = "com.google.dagger",
                artifactId = "hilt-android",
                category = DependencyCategory.DEPENDENCY_INJECTION,
                packageName = "dagger.hilt.android",
                detectionClass = "dagger.hilt.android.HiltAndroidApp",
                description = "DI library built on Dagger for Android",
                website = "https://dagger.dev/hilt/",
            ),
            LibrarySpec(
                name = "Kodein",
                groupId = "org.kodein.di",
                artifactId = "kodein-di",
                category = DependencyCategory.DEPENDENCY_INJECTION,
                packageName = "org.kodein.di",
                detectionClass = "org.kodein.di.DI",
                description = "Kotlin dependency injection framework",
                website = "https://kodein.org/di/",
            ),

            // ============ IMAGE LOADING ============
            LibrarySpec(
                name = "Coil",
                groupId = "io.coil-kt",
                artifactId = "coil",
                category = DependencyCategory.IMAGE_LOADING,
                packageName = "coil",
                detectionClass = "coil.ImageLoader",
                description = "Image loading library for Android backed by Kotlin Coroutines",
                website = "https://coil-kt.github.io/coil/",
                buildConfigClass = "coil.BuildConfig",
            ),
            LibrarySpec(
                name = "Glide",
                groupId = "com.github.bumptech.glide",
                artifactId = "glide",
                category = DependencyCategory.IMAGE_LOADING,
                packageName = "com.bumptech.glide",
                detectionClass = "com.bumptech.glide.Glide",
                description = "Fast and efficient image loading library",
                website = "https://bumptech.github.io/glide/",
                buildConfigClass = "com.bumptech.glide.BuildConfig",
            ),
            LibrarySpec(
                name = "Picasso",
                groupId = "com.squareup.picasso",
                artifactId = "picasso",
                category = DependencyCategory.IMAGE_LOADING,
                packageName = "com.squareup.picasso",
                detectionClass = "com.squareup.picasso.Picasso",
                description = "Image downloading and caching library by Square",
                website = "https://square.github.io/picasso/",
            ),
            LibrarySpec(
                name = "Fresco",
                groupId = "com.facebook.fresco",
                artifactId = "fresco",
                category = DependencyCategory.IMAGE_LOADING,
                packageName = "com.facebook.fresco",
                detectionClass = "com.facebook.fresco.Fresco",
                description = "Image management library by Meta",
                website = "https://frescolib.org/",
            ),

            // ============ SERIALIZATION ============
            LibrarySpec(
                name = "Gson",
                groupId = "com.google.code.gson",
                artifactId = "gson",
                category = DependencyCategory.SERIALIZATION,
                packageName = "com.google.gson",
                detectionClass = "com.google.gson.Gson",
                description = "JSON serialization/deserialization library by Google",
                website = "https://github.com/google/gson",
                customVersionExtractor = {
                    try {
                        val clazz = Class.forName("com.google.gson.internal.GsonBuildConfig")
                        val field = clazz.getDeclaredField("VERSION")
                        field.isAccessible = true
                        val version = field.get(null)?.toString()
                        version?.let { it to DetectionMethod.BUILD_CONFIG }
                    } catch (e: Exception) {
                        null
                    }
                },
            ),
            LibrarySpec(
                name = "Moshi",
                groupId = "com.squareup.moshi",
                artifactId = "moshi",
                category = DependencyCategory.SERIALIZATION,
                packageName = "com.squareup.moshi",
                detectionClass = "com.squareup.moshi.Moshi",
                description = "Modern JSON library for Android and Java by Square",
                website = "https://github.com/square/moshi",
            ),
            LibrarySpec(
                name = "Kotlinx Serialization",
                groupId = "org.jetbrains.kotlinx",
                artifactId = "kotlinx-serialization-json",
                category = DependencyCategory.SERIALIZATION,
                packageName = "kotlinx.serialization",
                detectionClass = "kotlinx.serialization.json.Json",
                description = "Kotlin multiplatform serialization library",
                website = "https://github.com/Kotlin/kotlinx.serialization",
            ),
            LibrarySpec(
                name = "Jackson",
                groupId = "com.fasterxml.jackson.core",
                artifactId = "jackson-databind",
                category = DependencyCategory.SERIALIZATION,
                packageName = "com.fasterxml.jackson.databind",
                detectionClass = "com.fasterxml.jackson.databind.ObjectMapper",
                description = "JSON processor for Java",
                website = "https://github.com/FasterXML/jackson",
            ),
            LibrarySpec(
                name = "Protocol Buffers",
                groupId = "com.google.protobuf",
                artifactId = "protobuf-java",
                category = DependencyCategory.SERIALIZATION,
                packageName = "com.google.protobuf",
                detectionClass = "com.google.protobuf.MessageLite",
                description = "Protocol Buffers by Google",
                website = "https://protobuf.dev/",
            ),

            // ============ DATABASE ============
            LibrarySpec(
                name = "Room",
                groupId = "androidx.room",
                artifactId = "room-runtime",
                category = DependencyCategory.DATABASE,
                packageName = "androidx.room",
                detectionClass = "androidx.room.Room",
                description = "SQLite abstraction layer by AndroidX",
                website = "https://developer.android.com/jetpack/androidx/releases/room",
            ),
            LibrarySpec(
                name = "Realm",
                groupId = "io.realm",
                artifactId = "realm-android",
                category = DependencyCategory.DATABASE,
                packageName = "io.realm",
                detectionClass = "io.realm.Realm",
                description = "Mobile database",
                website = "https://realm.io/",
            ),
            LibrarySpec(
                name = "SQLDelight",
                groupId = "app.cash.sqldelight",
                artifactId = "runtime",
                category = DependencyCategory.DATABASE,
                packageName = "app.cash.sqldelight",
                detectionClass = "app.cash.sqldelight.db.SqlDriver",
                description = "Typesafe SQL by Cash App",
                website = "https://cashapp.github.io/sqldelight/",
            ),
            LibrarySpec(
                name = "ObjectBox",
                groupId = "io.objectbox",
                artifactId = "objectbox-android",
                category = DependencyCategory.DATABASE,
                packageName = "io.objectbox",
                detectionClass = "io.objectbox.BoxStore",
                description = "Fast object database",
                website = "https://objectbox.io/",
            ),

            // ============ REACTIVE ============
            LibrarySpec(
                name = "RxJava 3",
                groupId = "io.reactivex.rxjava3",
                artifactId = "rxjava",
                category = DependencyCategory.REACTIVE,
                packageName = "io.reactivex.rxjava3",
                detectionClass = "io.reactivex.rxjava3.core.Observable",
                description = "Reactive Extensions for the JVM",
                website = "https://github.com/ReactiveX/RxJava",
            ),
            LibrarySpec(
                name = "RxJava 2",
                groupId = "io.reactivex.rxjava2",
                artifactId = "rxjava",
                category = DependencyCategory.REACTIVE,
                packageName = "io.reactivex",
                detectionClass = "io.reactivex.Observable",
                description = "Reactive Extensions for the JVM (v2)",
                website = "https://github.com/ReactiveX/RxJava",
            ),
            LibrarySpec(
                name = "RxAndroid",
                groupId = "io.reactivex.rxjava3",
                artifactId = "rxandroid",
                category = DependencyCategory.REACTIVE,
                packageName = "io.reactivex.rxjava3.android",
                detectionClass = "io.reactivex.rxjava3.android.schedulers.AndroidSchedulers",
                description = "RxJava bindings for Android",
                website = "https://github.com/ReactiveX/RxAndroid",
            ),
            LibrarySpec(
                name = "Kotlin Coroutines",
                groupId = "org.jetbrains.kotlinx",
                artifactId = "kotlinx-coroutines-core",
                category = DependencyCategory.REACTIVE,
                packageName = "kotlinx.coroutines",
                detectionClass = "kotlinx.coroutines.CoroutineScope",
                description = "Kotlin coroutines support library",
                website = "https://github.com/Kotlin/kotlinx.coroutines",
            ),
            LibrarySpec(
                name = "Kotlin Flow",
                groupId = "org.jetbrains.kotlinx",
                artifactId = "kotlinx-coroutines-core",
                category = DependencyCategory.REACTIVE,
                packageName = "kotlinx.coroutines.flow",
                detectionClass = "kotlinx.coroutines.flow.Flow",
                description = "Cold asynchronous data stream",
                website = "https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/",
            ),

            // ============ LOGGING ============
            LibrarySpec(
                name = "Timber",
                groupId = "com.jakewharton.timber",
                artifactId = "timber",
                category = DependencyCategory.LOGGING,
                packageName = "timber.log",
                detectionClass = "timber.log.Timber",
                description = "Extensible logging utility by Jake Wharton",
                website = "https://github.com/JakeWharton/timber",
            ),
            LibrarySpec(
                name = "SLF4J",
                groupId = "org.slf4j",
                artifactId = "slf4j-api",
                category = DependencyCategory.LOGGING,
                packageName = "org.slf4j",
                detectionClass = "org.slf4j.Logger",
                description = "Simple Logging Facade for Java",
                website = "https://www.slf4j.org/",
            ),

            // ============ ANALYTICS ============
            LibrarySpec(
                name = "Firebase Analytics",
                groupId = "com.google.firebase",
                artifactId = "firebase-analytics",
                category = DependencyCategory.ANALYTICS,
                packageName = "com.google.firebase.analytics",
                detectionClass = "com.google.firebase.analytics.FirebaseAnalytics",
                description = "Firebase Analytics SDK",
                website = "https://firebase.google.com/docs/analytics",
            ),
            LibrarySpec(
                name = "Firebase Crashlytics",
                groupId = "com.google.firebase",
                artifactId = "firebase-crashlytics",
                category = DependencyCategory.ANALYTICS,
                packageName = "com.google.firebase.crashlytics",
                detectionClass = "com.google.firebase.crashlytics.FirebaseCrashlytics",
                description = "Firebase Crashlytics SDK",
                website = "https://firebase.google.com/docs/crashlytics",
            ),

            // ============ UI FRAMEWORK ============
            LibrarySpec(
                name = "Jetpack Compose",
                groupId = "androidx.compose.ui",
                artifactId = "ui",
                category = DependencyCategory.UI_FRAMEWORK,
                packageName = "androidx.compose.ui",
                detectionClass = "androidx.compose.ui.Modifier",
                description = "Android's modern UI toolkit",
                website = "https://developer.android.com/jetpack/compose",
            ),
            LibrarySpec(
                name = "Material 3",
                groupId = "androidx.compose.material3",
                artifactId = "material3",
                category = DependencyCategory.UI_FRAMEWORK,
                packageName = "androidx.compose.material3",
                detectionClass = "androidx.compose.material3.MaterialTheme",
                description = "Material Design 3 for Compose",
                website = "https://m3.material.io/",
            ),
            LibrarySpec(
                name = "Accompanist",
                groupId = "com.google.accompanist",
                artifactId = "accompanist-systemuicontroller",
                category = DependencyCategory.UI_FRAMEWORK,
                packageName = "com.google.accompanist",
                detectionClass = "com.google.accompanist.systemuicontroller.SystemUiController",
                description = "Compose UI utilities by Google",
                website = "https://google.github.io/accompanist/",
            ),

            // ============ SECURITY ============
            LibrarySpec(
                name = "AndroidX Security Crypto",
                groupId = "androidx.security",
                artifactId = "security-crypto",
                category = DependencyCategory.SECURITY,
                packageName = "androidx.security.crypto",
                detectionClass = "androidx.security.crypto.EncryptedSharedPreferences",
                description = "AndroidX Security library for encryption",
                website = "https://developer.android.com/jetpack/androidx/releases/security",
            ),
            LibrarySpec(
                name = "Tink",
                groupId = "com.google.crypto.tink",
                artifactId = "tink-android",
                category = DependencyCategory.SECURITY,
                packageName = "com.google.crypto.tink",
                detectionClass = "com.google.crypto.tink.Aead",
                description = "Cryptographic library by Google",
                website = "https://developers.google.com/tink",
            ),

            // ============ UTILITY ============
            LibrarySpec(
                name = "LeakCanary",
                groupId = "com.squareup.leakcanary",
                artifactId = "leakcanary-android",
                category = DependencyCategory.UTILITY,
                packageName = "leakcanary",
                detectionClass = "leakcanary.LeakCanary",
                description = "Memory leak detection library",
                website = "https://square.github.io/leakcanary/",
            ),
            LibrarySpec(
                name = "Chucker",
                groupId = "com.github.chuckerteam.chucker",
                artifactId = "library",
                category = DependencyCategory.UTILITY,
                packageName = "com.chuckerteam.chucker",
                detectionClass = "com.chuckerteam.chucker.api.ChuckerInterceptor",
                description = "HTTP inspector for Android",
                website = "https://github.com/ChuckerTeam/chucker",
            ),
            LibrarySpec(
                name = "Hyperion",
                groupId = "com.willowtreeapps.hyperion",
                artifactId = "hyperion-core",
                category = DependencyCategory.UTILITY,
                packageName = "com.willowtreeapps.hyperion",
                detectionClass = "com.willowtreeapps.hyperion.core.Hyperion",
                description = "App debugging & inspection tool",
                website = "https://github.com/nickreimer/Hyperion-Android",
            ),
            LibrarySpec(
                name = "ThreeTenABP",
                groupId = "com.jakewharton.threetenabp",
                artifactId = "threetenabp",
                category = DependencyCategory.UTILITY,
                packageName = "com.jakewharton.threetenabp",
                detectionClass = "com.jakewharton.threetenabp.AndroidThreeTen",
                description = "JSR-310 backport for Android",
                website = "https://github.com/JakeWharton/ThreeTenABP",
            ),

            // ============ ANDROIDX ============
            LibrarySpec(
                name = "AndroidX Core KTX",
                groupId = "androidx.core",
                artifactId = "core-ktx",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.core",
                detectionClass = "androidx.core.content.ContextCompat",
                description = "Kotlin extensions for AndroidX Core",
                website = "https://developer.android.com/kotlin/ktx",
            ),
            LibrarySpec(
                name = "AndroidX AppCompat",
                groupId = "androidx.appcompat",
                artifactId = "appcompat",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.appcompat",
                detectionClass = "androidx.appcompat.app.AppCompatActivity",
                description = "Backward-compatible Android framework APIs",
                website = "https://developer.android.com/jetpack/androidx/releases/appcompat",
            ),
            LibrarySpec(
                name = "AndroidX Activity",
                groupId = "androidx.activity",
                artifactId = "activity-ktx",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.activity",
                detectionClass = "androidx.activity.ComponentActivity",
                description = "Access composable APIs in Activity",
                website = "https://developer.android.com/jetpack/androidx/releases/activity",
            ),
            LibrarySpec(
                name = "AndroidX Fragment",
                groupId = "androidx.fragment",
                artifactId = "fragment-ktx",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.fragment",
                detectionClass = "androidx.fragment.app.Fragment",
                description = "Segment your app into multiple screens",
                website = "https://developer.android.com/jetpack/androidx/releases/fragment",
            ),
            LibrarySpec(
                name = "AndroidX Lifecycle",
                groupId = "androidx.lifecycle",
                artifactId = "lifecycle-runtime-ktx",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.lifecycle",
                detectionClass = "androidx.lifecycle.ViewModel",
                description = "Lifecycle-aware components",
                website = "https://developer.android.com/jetpack/androidx/releases/lifecycle",
            ),
            LibrarySpec(
                name = "AndroidX Navigation",
                groupId = "androidx.navigation",
                artifactId = "navigation-compose",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.navigation",
                detectionClass = "androidx.navigation.NavController",
                description = "Navigation component",
                website = "https://developer.android.com/jetpack/androidx/releases/navigation",
            ),
            LibrarySpec(
                name = "AndroidX Paging",
                groupId = "androidx.paging",
                artifactId = "paging-runtime",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.paging",
                detectionClass = "androidx.paging.PagingSource",
                description = "Load data in pages",
                website = "https://developer.android.com/jetpack/androidx/releases/paging",
            ),
            LibrarySpec(
                name = "AndroidX Work Manager",
                groupId = "androidx.work",
                artifactId = "work-runtime-ktx",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.work",
                detectionClass = "androidx.work.WorkManager",
                description = "Schedule deferrable, asynchronous tasks",
                website = "https://developer.android.com/jetpack/androidx/releases/work",
            ),
            LibrarySpec(
                name = "AndroidX DataStore",
                groupId = "androidx.datastore",
                artifactId = "datastore-preferences",
                category = DependencyCategory.ANDROIDX,
                packageName = "androidx.datastore",
                detectionClass = "androidx.datastore.preferences.core.Preferences",
                description = "Data storage solution",
                website = "https://developer.android.com/jetpack/androidx/releases/datastore",
            ),

            // ============ KOTLIN ============
            LibrarySpec(
                name = "Kotlin Stdlib",
                groupId = "org.jetbrains.kotlin",
                artifactId = "kotlin-stdlib",
                category = DependencyCategory.KOTLIN,
                packageName = "kotlin",
                detectionClass = "kotlin.Unit",
                description = "Kotlin Standard Library",
                website = "https://kotlinlang.org/",
                customVersionExtractor = {
                    try {
                        val version = KotlinVersion.CURRENT.toString()
                        version to DetectionMethod.VERSION_FIELD
                    } catch (e: Exception) {
                        null
                    }
                },
            ),
            LibrarySpec(
                name = "Kotlin Reflect",
                groupId = "org.jetbrains.kotlin",
                artifactId = "kotlin-reflect",
                category = DependencyCategory.KOTLIN,
                packageName = "kotlin.reflect",
                detectionClass = "kotlin.reflect.KClass",
                description = "Kotlin Reflection Library",
                website = "https://kotlinlang.org/docs/reflection.html",
            ),

            // ============ TESTING ============
            LibrarySpec(
                name = "JUnit",
                groupId = "junit",
                artifactId = "junit",
                category = DependencyCategory.TESTING,
                packageName = "org.junit",
                detectionClass = "org.junit.Test",
                description = "Unit testing framework",
                website = "https://junit.org/",
            ),
            LibrarySpec(
                name = "Mockito",
                groupId = "org.mockito",
                artifactId = "mockito-core",
                category = DependencyCategory.TESTING,
                packageName = "org.mockito",
                detectionClass = "org.mockito.Mockito",
                description = "Mocking framework for unit tests",
                website = "https://site.mockito.org/",
            ),
            LibrarySpec(
                name = "MockK",
                groupId = "io.mockk",
                artifactId = "mockk",
                category = DependencyCategory.TESTING,
                packageName = "io.mockk",
                detectionClass = "io.mockk.MockK",
                description = "Mocking library for Kotlin",
                website = "https://mockk.io/",
            ),
            LibrarySpec(
                name = "Espresso",
                groupId = "androidx.test.espresso",
                artifactId = "espresso-core",
                category = DependencyCategory.TESTING,
                packageName = "androidx.test.espresso",
                detectionClass = "androidx.test.espresso.Espresso",
                description = "UI testing framework",
                website = "https://developer.android.com/training/testing/espresso",
            ),
        )
    }

    /**
     * Specification for a library to detect.
     */
    private data class LibrarySpec(
        val name: String,
        val groupId: String?,
        val artifactId: String?,
        val category: DependencyCategory,
        val packageName: String,
        val detectionClass: String,
        val description: String,
        val website: String?,
        val versionClass: String? = null,
        val versionField: String? = null,
        val buildConfigClass: String? = null,
        val customVersionExtractor: (() -> Pair<String, DetectionMethod>?)? = null,
    ) {
        fun toUndetected() = DependencyInfo(
            name = name,
            groupId = groupId,
            artifactId = artifactId,
            version = null,
            category = category,
            detectionMethod = DetectionMethod.CLASS_PRESENCE_ONLY,
            packageName = packageName,
            isDetected = false,
            description = description,
            website = website,
        )
    }
}
