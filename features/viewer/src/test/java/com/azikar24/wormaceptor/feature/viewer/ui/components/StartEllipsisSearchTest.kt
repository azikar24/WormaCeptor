package com.azikar24.wormaceptor.feature.viewer.ui.components

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class StartEllipsisSearchTest {

    @Test
    fun `starts cover every char of plain text plus the end`() {
        graphemeStarts("abc").toList() shouldBe listOf(0, 1, 2, 3)
    }

    @Test
    fun `starts never split a surrogate pair`() {
        val text = "a😀b"

        graphemeStarts(text).toList() shouldBe listOf(0, 1, 3, 4)
    }

    @Test
    fun `starts keep a combining mark with its base`() {
        val text = "éx"

        graphemeStarts(text).toList() shouldBe listOf(0, 2, 3)
    }

    @Test
    fun `search returns the smallest start whose suffix fits`() {
        val text = "/api/v1/users/12345"
        val maxChars = 8

        val start = smallestFittingStart(graphemeStarts(text)) { text.length - it <= maxChars }

        text.substring(start) shouldBe "rs/12345"
    }

    @Test
    fun `search on right-to-left text uses widths, not positions`() {
        val text = "/مسار/طويل/جدا"
        val maxChars = 5

        val start = smallestFittingStart(graphemeStarts(text)) { text.length - it <= maxChars }

        start shouldBe text.length - maxChars
    }

    @Test
    fun `search falls back to the empty suffix`() {
        val text = "abc"

        smallestFittingStart(graphemeStarts(text)) { it == text.length } shouldBe 3
    }

    @Test
    fun `search measures logarithmically many suffixes`() {
        val text = "x".repeat(1024)
        var calls = 0

        smallestFittingStart(graphemeStarts(text)) {
            calls++
            text.length - it <= 10
        }

        (calls <= 11) shouldBe true
    }
}
