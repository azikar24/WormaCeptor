package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CrashTest {

    private fun defaultCrash() = Crash(
        id = 1,
        timestamp = 1_700_000_000_000L,
        exceptionType = "java.lang.NullPointerException",
        message = "Attempt to invoke method on null reference",
        stackTrace = "at com.example.App.onCreate(App.kt:42)\nat android.app.Activity.performCreate(Activity.java:8000)",
    )

    @Nested
    inner class Construction {

        @Test
        fun `constructs with all fields`() {
            val crash = defaultCrash()

            crash.id shouldBe 1
            crash.timestamp shouldBe 1_700_000_000_000L
            crash.exceptionType shouldBe "java.lang.NullPointerException"
            crash.message shouldBe "Attempt to invoke method on null reference"
            crash.stackTrace shouldBe "at com.example.App.onCreate(App.kt:42)\nat android.app.Activity.performCreate(Activity.java:8000)"
        }

        @Test
        fun `constructs with null message`() {
            val crash = defaultCrash().copy(message = null)

            crash.message shouldBe null
        }
    }

    @Nested
    inner class Defaults {

        @Test
        fun `id defaults to 0`() {
            val crash = Crash(
                timestamp = 0L,
                exceptionType = "Exception",
                message = null,
                stackTrace = "",
            )

            crash.id shouldBe 0
        }
    }

    @Nested
    inner class EqualityAndCopy {

        @Test
        fun `equal instances have the same hashCode`() {
            val c1 = defaultCrash()
            val c2 = defaultCrash()

            c1 shouldBe c2
            c1.hashCode() shouldBe c2.hashCode()
        }

        @Test
        fun `different id makes instances unequal`() {
            val c1 = defaultCrash()
            val c2 = defaultCrash().copy(id = 2)

            c1 shouldNotBe c2
        }

        @Test
        fun `copy preserves unchanged fields`() {
            val original = defaultCrash()
            val copied = original.copy(exceptionType = "java.io.IOException")

            copied.id shouldBe original.id
            copied.timestamp shouldBe original.timestamp
            copied.exceptionType shouldBe "java.io.IOException"
        }
    }
}
