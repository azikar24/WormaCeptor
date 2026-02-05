/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.content.Context
import android.content.pm.PackageManager
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.Locale
import java.util.zip.ZipFile

/**
 * Engine that inspects and reports all libraries loaded into the application process.
 *
 * Features:
 * - Reads /proc/self/maps to discover loaded native libraries
 * - Parses APK to find DEX files
 * - Identifies system vs app libraries
 * - Provides filtering and search capabilities
 *
 * @param context Application context for accessing package info
 */
class LoadedLibrariesEngine(
    private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // All loaded libraries
    private val _libraries = MutableStateFlow<List<LoadedLibrary>>(emptyList())
    val libraries: StateFlow<List<LoadedLibrary>> = _libraries.asStateFlow()

    // Summary statistics
    private val _summary = MutableStateFlow(LibrarySummary.empty())
    val summary: StateFlow<LibrarySummary> = _summary.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Cache for package info
    private val packageName: String = context.packageName
    private val appSourceDir: String by lazy {
        try {
            context.packageManager.getApplicationInfo(packageName, 0).sourceDir
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }
    private val appNativeLibDir: String by lazy {
        try {
            context.packageManager.getApplicationInfo(packageName, 0).nativeLibraryDir ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    init {
        // Initial scan
        refresh()
    }

    /**
     * Refreshes the list of loaded libraries.
     * Scans /proc/self/maps and APK contents.
     */
    fun refresh() {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val allLibraries = mutableListOf<LoadedLibrary>()

                // Collect native libraries from /proc/self/maps
                val nativeLibs = withContext(Dispatchers.IO) {
                    parseNativeLibraries()
                }
                allLibraries.addAll(nativeLibs)

                // Collect DEX files from APK
                val dexFiles = withContext(Dispatchers.IO) {
                    parseDexFiles()
                }
                allLibraries.addAll(dexFiles)

                // Collect JAR dependencies
                val jarFiles = withContext(Dispatchers.IO) {
                    parseJarDependencies()
                }
                allLibraries.addAll(jarFiles)

                // Remove duplicates by path
                val uniqueLibraries = allLibraries.distinctBy { it.path }

                _libraries.value = uniqueLibraries
                _summary.value = calculateSummary(uniqueLibraries)
            } catch (e: Exception) {
                _error.value = "Failed to scan libraries: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Returns libraries filtered by type.
     */
    fun getLibrariesByType(type: LoadedLibrary.LibraryType): List<LoadedLibrary> {
        return _libraries.value.filter { it.type == type }
    }

    /**
     * Searches libraries by name or path.
     */
    fun searchLibraries(query: String): List<LoadedLibrary> {
        if (query.isBlank()) return _libraries.value

        val lowerQuery = query.lowercase()
        return _libraries.value.filter { lib ->
            lib.name.lowercase().contains(lowerQuery) ||
                lib.path.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Parses /proc/self/maps to find loaded native libraries.
     */
    private fun parseNativeLibraries(): List<LoadedLibrary> {
        val libraries = mutableMapOf<String, LoadedLibrary>()

        try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists() || !mapsFile.canRead()) {
                return emptyList()
            }

            BufferedReader(FileReader(mapsFile)).use { reader ->
                reader.lineSequence().forEach { line ->
                    parseMapLine(line)?.let { lib ->
                        // Keep the first occurrence (with load address)
                        if (!libraries.containsKey(lib.path)) {
                            libraries[lib.path] = lib
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Silently handle permission issues
        }

        return libraries.values.toList()
    }

    /**
     * Parses a single line from /proc/self/maps.
     *
     * Format: address perms offset dev inode pathname
     * Example: 7f8e900000-7f8e901000 r-xp 00000000 fd:01 12345 /system/lib64/libc.so
     */
    private fun parseMapLine(line: String): LoadedLibrary? {
        val parts = line.trim().split(Regex("\\s+"), limit = 6)
        if (parts.size < 6) return null

        val path = parts[5]

        // Filter to only .so files
        if (!path.endsWith(".so") && !path.contains(".so.")) return null

        // Skip virtual mappings and anonymous regions
        if (path.startsWith("[") || path.isBlank()) return null

        val name = path.substringAfterLast("/")
        val addressRange = parts[0]
        val loadAddress = addressRange.substringBefore("-")

        // Determine if system library
        val isSystemLibrary = isSystemPath(path)

        // Get file size
        val size = try {
            val file = File(path)
            if (file.exists()) file.length() else null
        } catch (e: Exception) {
            null
        }

        return LoadedLibrary(
            name = name,
            path = path,
            type = LoadedLibrary.LibraryType.NATIVE_SO,
            size = size,
            loadAddress = "0x$loadAddress",
            version = extractVersionFromName(name),
            isSystemLibrary = isSystemLibrary,
        )
    }

    /**
     * Parses DEX files from the application APK.
     */
    private fun parseDexFiles(): List<LoadedLibrary> {
        val dexFiles = mutableListOf<LoadedLibrary>()

        if (appSourceDir.isBlank()) return dexFiles

        try {
            ZipFile(appSourceDir).use { zipFile ->
                zipFile.entries().asSequence()
                    .filter { entry ->
                        entry.name.endsWith(".dex") ||
                            (entry.name.startsWith("classes") && entry.name.endsWith(".dex"))
                    }
                    .forEach { entry ->
                        dexFiles.add(
                            LoadedLibrary(
                                name = entry.name,
                                path = "$appSourceDir!/${entry.name}",
                                type = LoadedLibrary.LibraryType.DEX,
                                size = entry.size.takeIf { it > 0 },
                                loadAddress = null,
                                version = null,
                                isSystemLibrary = false,
                            ),
                        )
                    }
            }
        } catch (e: Exception) {
            // APK might be inaccessible
        }

        return dexFiles
    }

    /**
     * Parses JAR dependencies from classpath and data directories.
     */
    private fun parseJarDependencies(): List<LoadedLibrary> {
        val jarFiles = mutableListOf<LoadedLibrary>()

        // Check common locations for JAR files
        val searchPaths = listOf(
            context.applicationInfo.dataDir,
            context.filesDir.absolutePath,
            context.cacheDir.absolutePath,
        )

        searchPaths.forEach { basePath ->
            try {
                val dir = File(basePath)
                if (dir.exists() && dir.isDirectory) {
                    findJarFiles(dir, jarFiles)
                }
            } catch (e: Exception) {
                // Skip inaccessible directories
            }
        }

        return jarFiles
    }

    /**
     * Recursively finds JAR files in a directory.
     */
    private fun findJarFiles(dir: File, result: MutableList<LoadedLibrary>, depth: Int = 0) {
        if (depth > 3) return // Limit recursion depth

        try {
            dir.listFiles()?.forEach { file ->
                when {
                    file.isFile && file.name.endsWith(".jar") -> {
                        result.add(
                            LoadedLibrary(
                                name = file.name,
                                path = file.absolutePath,
                                type = LoadedLibrary.LibraryType.JAR,
                                size = file.length(),
                                loadAddress = null,
                                version = extractVersionFromName(file.name),
                                isSystemLibrary = false,
                            ),
                        )
                    }
                    file.isDirectory -> {
                        findJarFiles(file, result, depth + 1)
                    }
                }
            }
        } catch (e: Exception) {
            // Skip inaccessible directories
        }
    }

    /**
     * Determines if a path refers to a system library.
     */
    private fun isSystemPath(path: String): Boolean {
        val systemPaths = listOf(
            "/system/",
            "/vendor/",
            "/apex/",
            "/product/",
            "/system_ext/",
            "/odm/",
        )
        return systemPaths.any { path.startsWith(it) }
    }

    /**
     * Extracts version from library name if present.
     * Examples: libfoo.so.1.2.3 -> 1.2.3, libfoo-1.0.so -> 1.0
     */
    private fun extractVersionFromName(name: String): String? {
        // Pattern: name.so.version
        val soVersionPattern = Regex("""\.so\.(\d+(?:\.\d+)*)$""")
        soVersionPattern.find(name)?.let {
            return it.groupValues[1]
        }

        // Pattern: name-version.so or name_version.so
        val dashVersionPattern = Regex("""[-_](\d+(?:\.\d+)*)\.[^.]+$""")
        dashVersionPattern.find(name)?.let {
            return it.groupValues[1]
        }

        return null
    }

    /**
     * Calculates summary statistics from the library list.
     */
    private fun calculateSummary(libraries: List<LoadedLibrary>): LibrarySummary {
        var nativeSoCount = 0
        var dexCount = 0
        var jarCount = 0
        var aarCount = 0
        var totalSize = 0L
        var systemCount = 0
        var appCount = 0

        libraries.forEach { lib ->
            when (lib.type) {
                LoadedLibrary.LibraryType.NATIVE_SO -> nativeSoCount++
                LoadedLibrary.LibraryType.DEX -> dexCount++
                LoadedLibrary.LibraryType.JAR -> jarCount++
                LoadedLibrary.LibraryType.AAR_RESOURCE -> aarCount++
            }

            lib.size?.let { totalSize += it }

            if (lib.isSystemLibrary) {
                systemCount++
            } else {
                appCount++
            }
        }

        return LibrarySummary(
            totalLibraries = libraries.size,
            nativeSoCount = nativeSoCount,
            dexCount = dexCount,
            jarCount = jarCount + aarCount,
            totalSizeBytes = totalSize,
            systemLibraryCount = systemCount,
            appLibraryCount = appCount,
        )
    }

    /**
     * Exports library information as a text report.
     */
    fun exportAsText(): String {
        val sb = StringBuilder()
        val summary = _summary.value
        val libraries = _libraries.value

        sb.appendLine("=== Loaded Libraries Report ===")
        sb.appendLine(
            "Generated: ${java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.US,
            ).format(java.util.Date())}",
        )
        sb.appendLine("Package: $packageName")
        sb.appendLine()

        sb.appendLine("=== Summary ===")
        sb.appendLine("Total Libraries: ${summary.totalLibraries}")
        sb.appendLine("Native (.so): ${summary.nativeSoCount}")
        sb.appendLine("DEX: ${summary.dexCount}")
        sb.appendLine("JAR: ${summary.jarCount}")
        sb.appendLine("System Libraries: ${summary.systemLibraryCount}")
        sb.appendLine("App Libraries: ${summary.appLibraryCount}")
        sb.appendLine("Total Size: ${formatSize(summary.totalSizeBytes)}")
        sb.appendLine()

        sb.appendLine("=== Native Libraries (.so) ===")
        libraries.filter { it.type == LoadedLibrary.LibraryType.NATIVE_SO }
            .sortedBy { it.name }
            .forEach { lib ->
                sb.appendLine("${lib.name}")
                sb.appendLine("  Path: ${lib.path}")
                lib.loadAddress?.let { sb.appendLine("  Load Address: $it") }
                lib.size?.let { sb.appendLine("  Size: ${formatSize(it)}") }
                lib.version?.let { sb.appendLine("  Version: $it") }
                sb.appendLine("  Type: ${if (lib.isSystemLibrary) "System" else "App"}")
                sb.appendLine()
            }

        sb.appendLine("=== DEX Files ===")
        libraries.filter { it.type == LoadedLibrary.LibraryType.DEX }
            .sortedBy { it.name }
            .forEach { lib ->
                sb.appendLine("${lib.name}")
                sb.appendLine("  Path: ${lib.path}")
                lib.size?.let { sb.appendLine("  Size: ${formatSize(it)}") }
                sb.appendLine()
            }

        sb.appendLine("=== JAR Files ===")
        libraries.filter { it.type == LoadedLibrary.LibraryType.JAR }
            .sortedBy { it.name }
            .forEach { lib ->
                sb.appendLine("${lib.name}")
                sb.appendLine("  Path: ${lib.path}")
                lib.size?.let { sb.appendLine("  Size: ${formatSize(it)}") }
                lib.version?.let { sb.appendLine("  Version: $it") }
                sb.appendLine()
            }

        return sb.toString()
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> String.format(Locale.US, "%.2f GB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> String.format(Locale.US, "%.2f MB", bytes / 1_048_576.0)
            bytes >= 1_024 -> String.format(Locale.US, "%.2f KB", bytes / 1_024.0)
            else -> "$bytes B"
        }
    }
}
