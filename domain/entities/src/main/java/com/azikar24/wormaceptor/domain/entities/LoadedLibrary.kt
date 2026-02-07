package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a library loaded into the application's process.
 *
 * @property name The library file name (e.g., "libc.so", "classes.dex")
 * @property path Full path to the library file
 * @property type Type of the library (native .so, DEX, JAR, etc.)
 * @property size Size of the library file in bytes, if available
 * @property loadAddress Memory address where native library is loaded, if applicable
 * @property version Version string if available from library metadata
 * @property isSystemLibrary Whether this is a system library vs app library
 */
data class LoadedLibrary(
    val name: String,
    val path: String,
    val type: LibraryType,
    val size: Long?,
    val loadAddress: String?,
    val version: String?,
    val isSystemLibrary: Boolean,
) {
    /**
     * Type of loaded library.
     */
    enum class LibraryType {
        /** Native shared object (.so) file */
        NATIVE_SO,

        /** Dalvik executable (.dex) file */
        DEX,

        /** Java archive (.jar) file */
        JAR,

        /** Resource from AAR (Android Archive) */
        AAR_RESOURCE,
    }

    companion object {
        /**
         * Creates an empty LoadedLibrary instance.
         */
        fun empty() = LoadedLibrary(
            name = "",
            path = "",
            type = LibraryType.NATIVE_SO,
            size = null,
            loadAddress = null,
            version = null,
            isSystemLibrary = false,
        )
    }
}

/**
 * Summary statistics for loaded libraries.
 *
 * @property totalLibraries Total number of libraries loaded
 * @property nativeSoCount Number of native .so libraries
 * @property dexCount Number of DEX files
 * @property jarCount Number of JAR files
 * @property totalSizeBytes Total size of all libraries in bytes
 * @property systemLibraryCount Number of system libraries
 * @property appLibraryCount Number of app libraries
 */
data class LibrarySummary(
    val totalLibraries: Int,
    val nativeSoCount: Int,
    val dexCount: Int,
    val jarCount: Int,
    val totalSizeBytes: Long,
    val systemLibraryCount: Int,
    val appLibraryCount: Int,
) {
    companion object {
        /**
         * Creates an empty LibrarySummary instance.
         */
        fun empty() = LibrarySummary(
            totalLibraries = 0,
            nativeSoCount = 0,
            dexCount = 0,
            jarCount = 0,
            totalSizeBytes = 0L,
            systemLibraryCount = 0,
            appLibraryCount = 0,
        )
    }
}
