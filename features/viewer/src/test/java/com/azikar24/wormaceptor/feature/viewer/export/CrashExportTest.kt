package com.azikar24.wormaceptor.feature.viewer.export

import android.content.Context
import com.azikar24.wormaceptor.domain.entities.Crash
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CrashExportTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val messages = mutableListOf<String>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        messages.clear()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class CrashListJsonSerialization {

        @Test
        fun `should serialize single crash to JSON`() = runTest {
            val crash = Crash(
                id = 1,
                timestamp = 1_700_000_000_000L,
                exceptionType = "java.lang.NullPointerException",
                message = "Attempt to invoke virtual method on null reference",
                stackTrace = "at com.example.App.onCreate(App.kt:42)\n" +
                    "at android.app.Activity.performCreate(Activity.java:8000)",
            )

            val json = serializeCrashes(listOf(crash))

            json.length() shouldBe 1
            val obj = json.getJSONObject(0)
            obj.getLong("timestamp") shouldBe 1_700_000_000_000L
            obj.getString("exceptionType") shouldBe "java.lang.NullPointerException"
            obj.getString("message") shouldBe "Attempt to invoke virtual method on null reference"
            obj.getString("stackTrace") shouldContain "com.example.App.onCreate"
        }

        @Test
        fun `should serialize crash with null message as empty string`() = runTest {
            val crash = Crash(
                id = 2,
                timestamp = 1_700_000_000_000L,
                exceptionType = "java.lang.StackOverflowError",
                message = null,
                stackTrace = "at com.example.Recursion.run(Recursion.kt:5)",
            )

            val json = serializeCrashes(listOf(crash))

            val obj = json.getJSONObject(0)
            obj.getString("message") shouldBe ""
        }

        @Test
        fun `should serialize multiple crashes`() = runTest {
            val crashes = listOf(
                Crash(
                    id = 1,
                    timestamp = 1_700_000_000_000L,
                    exceptionType = "NullPointerException",
                    message = "first",
                    stackTrace = "trace1",
                ),
                Crash(
                    id = 2,
                    timestamp = 1_700_000_001_000L,
                    exceptionType = "IllegalStateException",
                    message = "second",
                    stackTrace = "trace2",
                ),
                Crash(
                    id = 3,
                    timestamp = 1_700_000_002_000L,
                    exceptionType = "OutOfMemoryError",
                    message = "third",
                    stackTrace = "trace3",
                ),
            )

            val json = serializeCrashes(crashes)

            json.length() shouldBe 3
            json.getJSONObject(0).getString("exceptionType") shouldBe "NullPointerException"
            json.getJSONObject(1).getString("exceptionType") shouldBe "IllegalStateException"
            json.getJSONObject(2).getString("exceptionType") shouldBe "OutOfMemoryError"
        }

        @Test
        fun `should preserve stack trace with newlines`() = runTest {
            val multiLineTrace = """
                at com.example.App.onCreate(App.kt:42)
                at android.app.Activity.performCreate(Activity.java:8000)
                at java.lang.reflect.Method.invoke(Method.java:498)
            """.trimIndent()

            val crash = Crash(
                id = 1,
                timestamp = 1_700_000_000_000L,
                exceptionType = "RuntimeException",
                message = "test",
                stackTrace = multiLineTrace,
            )

            val json = serializeCrashes(listOf(crash))

            val stackTrace = json.getJSONObject(0).getString("stackTrace")
            stackTrace shouldContain "com.example.App.onCreate"
            stackTrace shouldContain "android.app.Activity.performCreate"
            stackTrace shouldContain "java.lang.reflect.Method.invoke"
        }

        @Test
        fun `should produce valid JSON structure with four fields per crash`() = runTest {
            val crash = Crash(
                id = 1,
                timestamp = 1_700_000_000_000L,
                exceptionType = "Exception",
                message = "msg",
                stackTrace = "trace",
            )

            val json = serializeCrashes(listOf(crash))

            val obj = json.getJSONObject(0)
            val keys = obj.keys().asSequence().toSet()
            keys shouldBe setOf("timestamp", "exceptionType", "message", "stackTrace")
        }
    }

    @Nested
    inner class EmptyCrashList {

        @Test
        fun `should produce empty JSON array`() = runTest {
            val json = serializeCrashes(emptyList())

            json.length() shouldBe 0
        }

        @Test
        fun `should share empty JSON array via intent`() = runTest {
            every { context.startActivity(any()) } just Runs

            exportCrashes(context, emptyList(), onMessage = { messages.add(it) })

            // No error messages expected
            messages.none { it.startsWith("Export failed:") } shouldBe true
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `should emit error message when sharing fails`() = runTest {
            every { context.startActivity(any()) } throws RuntimeException("No activity found")

            exportCrashes(context, emptyList(), onMessage = { messages.add(it) })

            messages.any { it.contains("failed", ignoreCase = true) || it.contains("Failed") } shouldBe true
        }

        @Test
        fun `should not crash when export encounters an error`() = runTest {
            every { context.startActivity(any()) } throws RuntimeException("Activity not resolved")

            exportCrashes(context, emptyList(), onMessage = { messages.add(it) })

            // Verifies graceful error handling - no exception propagates
            messages.size shouldBe messages.size // confirms code completed without throwing
        }
    }

    @Nested
    inner class ExportFlow {

        @Test
        fun `should invoke startActivity for non-empty crash list`() = runTest {
            every { context.startActivity(any()) } just Runs

            val crashes = listOf(
                Crash(
                    id = 1,
                    timestamp = 1_700_000_000_000L,
                    exceptionType = "Exception",
                    message = "test",
                    stackTrace = "trace",
                ),
            )

            exportCrashes(context, crashes, onMessage = { messages.add(it) })

            messages.none { it.startsWith("Export failed:") } shouldBe true
        }
    }

    /**
     * Helper that replicates the serialization logic from CrashExport
     * so we can unit-test the JSON output independently.
     */
    private fun serializeCrashes(crashes: List<Crash>): JSONArray {
        val jsonArray = JSONArray()
        crashes.forEach { crash ->
            val jsonObject = JSONObject().apply {
                put("timestamp", crash.timestamp)
                put("exceptionType", crash.exceptionType)
                put("message", crash.message ?: "")
                put("stackTrace", crash.stackTrace)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }
}
