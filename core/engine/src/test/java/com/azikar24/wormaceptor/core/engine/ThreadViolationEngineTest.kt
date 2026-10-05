package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ThreadViolationEngineTest {

    private val engine = ThreadViolationEngine().apply {
        // applicationIdSuffix: app id no longer matches the code package
        configure(hostPackage = "com.example.app.debug")
    }

    private fun violationWith(vararg frames: String) = ThreadViolation(
        id = 1,
        timestamp = 0,
        violationType = ThreadViolation.ViolationType.DISK_READ,
        description = "",
        stackTrace = frames.toList(),
        durationMs = null,
        threadName = "main",
    )

    @Test
    fun `host frame is relevant even when application id has a suffix`() {
        val violation = violationWith(
            "android.os.StrictMode\$AndroidBlockGuardPolicy.onReadFromDisk(StrictMode.java:1)",
            "java.io.FileInputStream.<init>(FileInputStream.java:2)",
            "com.example.app.MainActivity.onCreate(MainActivity.kt:3)",
        )

        engine.isRelevantViolation(violation) shouldBe true
    }

    @Test
    fun `system-only stack is not relevant`() {
        val violation = violationWith(
            "android.os.StrictMode\$AndroidBlockGuardPolicy.onReadFromDisk(StrictMode.java:1)",
            "androidx.core.content.ContextCompat.getDrawable(ContextCompat.java:2)",
            "java.io.FileInputStream.<init>(FileInputStream.java:3)",
            "com.android.internal.os.ZygoteInit.main(ZygoteInit.java:4)",
        )

        engine.isRelevantViolation(violation) shouldBe false
    }

    @Test
    fun `package containing android segment is not mistaken for system`() {
        val violation = violationWith(
            "android.os.StrictMode\$AndroidBlockGuardPolicy.onReadFromDisk(StrictMode.java:1)",
            "com.example.android.storage.Cache.load(Cache.kt:2)",
        )

        engine.isRelevantViolation(violation) shouldBe true
    }
}
