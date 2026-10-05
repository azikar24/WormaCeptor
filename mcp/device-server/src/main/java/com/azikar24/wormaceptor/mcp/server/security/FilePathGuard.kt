package com.azikar24.wormaceptor.mcp.server.security

import java.io.File
import java.io.IOException

/**
 * Resolves [path] and returns it only if it stays inside one of [allowedRoots].
 *
 * Relative paths resolve against [baseDir] (the app's data directory). Paths are
 * canonicalized first, so `..` segments and symlinks pointing outside the roots are rejected.
 */
internal fun resolveWithinRoots(
    path: String,
    baseDir: File,
    allowedRoots: List<File>,
): File? {
    val candidate = File(path).let { if (it.isAbsolute) it else File(baseDir, path) }
    return try {
        val canonical = candidate.canonicalFile
        val inside = allowedRoots.any { root ->
            val canonicalRoot = root.canonicalFile
            canonical == canonicalRoot || canonical.startsWith(canonicalRoot)
        }
        if (inside) canonical else null
    } catch (_: IOException) {
        null
    }
}
