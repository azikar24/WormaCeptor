package com.azikar24.wormaceptor.api.internal

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MainProcessTest {

    @Test
    fun `matches the default process name`() {
        isMainProcess("com.example.app", "com.example.app") shouldBe true
    }

    @Test
    fun `rejects a secondary process`() {
        isMainProcess("com.example.app:remote", "com.example.app") shouldBe false
    }

    @Test
    fun `rejects an unknown process name`() {
        isMainProcess(null, "com.example.app") shouldBe false
    }

    @Test
    fun `honors a custom default process name`() {
        isMainProcess("com.example.custom", "com.example.custom") shouldBe true
    }
}
