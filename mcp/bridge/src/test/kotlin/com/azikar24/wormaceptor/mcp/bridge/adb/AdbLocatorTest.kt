package com.azikar24.wormaceptor.mcp.bridge.adb

import org.junit.Assert.assertEquals
import org.junit.Test

class AdbLocatorTest {

    private fun resolve(
        flag: String? = null,
        env: Map<String, String> = emptyMap(),
        files: Set<String> = emptySet(),
        os: String = "Mac OS X",
        home: String = "/Users/dev",
    ) = resolveAdbPath(flag, env::get, files::contains, os, home)

    @Test
    fun `flag wins even when nothing exists`() {
        assertEquals("/opt/adb", resolve(flag = "/opt/adb", env = mapOf("ANDROID_HOME" to "/sdk")))
    }

    @Test
    fun `ANDROID_HOME comes before ANDROID_SDK_ROOT and PATH`() {
        val files = setOf("/home/sdk/platform-tools/adb", "/root/sdk/platform-tools/adb", "/usr/bin/adb")
        val env = mapOf("ANDROID_HOME" to "/home/sdk", "ANDROID_SDK_ROOT" to "/root/sdk", "PATH" to "/usr/bin")
        assertEquals("/home/sdk/platform-tools/adb", resolve(env = env, files = files))
    }

    @Test
    fun `ANDROID_SDK_ROOT is used when ANDROID_HOME has no adb`() {
        val env = mapOf("ANDROID_HOME" to "/empty", "ANDROID_SDK_ROOT" to "/root/sdk")
        assertEquals("/root/sdk/platform-tools/adb", resolve(env = env, files = setOf("/root/sdk/platform-tools/adb")))
    }

    @Test
    fun `PATH is searched in order before the default SDK dir`() {
        val env = mapOf("PATH" to "/bin:/usr/local/bin:/usr/bin")
        val files = setOf("/usr/local/bin/adb", "/usr/bin/adb", "/Users/dev/Library/Android/sdk/platform-tools/adb")
        assertEquals("/usr/local/bin/adb", resolve(env = env, files = files))
    }

    @Test
    fun `default SDK dir per OS`() {
        assertEquals(
            "/Users/dev/Library/Android/sdk/platform-tools/adb",
            resolve(files = setOf("/Users/dev/Library/Android/sdk/platform-tools/adb")),
        )
        assertEquals(
            "/home/dev/Android/Sdk/platform-tools/adb",
            resolve(os = "Linux", home = "/home/dev", files = setOf("/home/dev/Android/Sdk/platform-tools/adb")),
        )
    }

    @Test
    fun `windows uses adb_exe, semicolon PATH and LOCALAPPDATA`() {
        val sdkAdb = "C:\\Users\\dev\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe"
        assertEquals(
            sdkAdb,
            resolve(
                os = "Windows 11",
                env = mapOf("LOCALAPPDATA" to "C:\\Users\\dev\\AppData\\Local", "PATH" to "C:\\a;C:\\b"),
                files = setOf(sdkAdb),
            ),
        )
        assertEquals(
            "C:\\b\\adb.exe",
            resolve(os = "Windows 11", env = mapOf("PATH" to "C:\\a;C:\\b"), files = setOf("C:\\b\\adb.exe")),
        )
    }

    @Test
    fun `falls back to the bare executable name when nothing is found`() {
        assertEquals("adb", resolve(env = mapOf("PATH" to "/bin")))
        assertEquals("adb.exe", resolve(os = "Windows 10"))
    }
}
