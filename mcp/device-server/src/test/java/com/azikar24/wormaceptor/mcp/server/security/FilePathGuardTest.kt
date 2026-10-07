package com.azikar24.wormaceptor.mcp.server.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

class FilePathGuardTest {

    @TempDir
    lateinit var temp: File

    private val dataDir by lazy { File(temp, "data").apply { mkdirs() } }
    private val filesDir by lazy { File(dataDir, "files").apply { mkdirs() } }
    private val externalDir by lazy { File(temp, "external").apply { mkdirs() } }
    private val roots by lazy { listOf(dataDir, externalDir) }

    @Test
    fun `relative paths resolve against the data directory`() {
        val config = File(filesDir, "config.json").apply { writeText("{}") }
        assertEquals(config.canonicalFile, resolveWithinRoots("files/config.json", dataDir, roots))
    }

    @Test
    fun `absolute paths inside any root are allowed`() {
        assertEquals(externalDir.canonicalFile, resolveWithinRoots(externalDir.path, dataDir, roots))
        assertEquals(filesDir.canonicalFile, resolveWithinRoots(filesDir.absolutePath, dataDir, roots))
    }

    @Test
    fun `traversal and outside absolute paths are rejected`() {
        assertNull(resolveWithinRoots("../outside.txt", dataDir, roots))
        assertNull(resolveWithinRoots("files/../../outside.txt", dataDir, roots))
        assertNull(resolveWithinRoots("/etc/hosts", dataDir, roots))
    }

    @Test
    fun `sibling directory sharing a name prefix is rejected`() {
        val sibling = File(temp, "data2").apply { mkdirs() }
        assertNull(resolveWithinRoots(sibling.path, dataDir, roots))
    }

    @Test
    fun `symlink escaping the root is rejected`() {
        val secret = File(temp, "secret.txt").apply { writeText("x") }
        val link = File(filesDir, "link.txt")
        Files.createSymbolicLink(link.toPath(), secret.toPath())
        assertNull(resolveWithinRoots("files/link.txt", dataDir, roots))
    }
}
