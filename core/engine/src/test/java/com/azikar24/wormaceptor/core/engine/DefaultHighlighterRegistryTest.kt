package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.SyntaxHighlighter
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DefaultHighlighterRegistryTest {

    private lateinit var registry: DefaultHighlighterRegistry

    @BeforeEach
    fun setUp() {
        registry = DefaultHighlighterRegistry()
    }

    @Nested
    inner class RegisterAndGetHighlighter {

        @Test
        fun `should register and retrieve highlighter by language`() {
            val jsonHighlighter = createMockHighlighter("json")
            registry.register(jsonHighlighter)

            registry.getHighlighter("json") shouldBe jsonHighlighter
        }

        @Test
        fun `should return null for unregistered language`() {
            registry.getHighlighter("python") shouldBe null
        }

        @Test
        fun `should support case-insensitive lookup`() {
            val jsonHighlighter = createMockHighlighter("json")
            registry.register(jsonHighlighter)

            registry.getHighlighter("JSON") shouldBe jsonHighlighter
            registry.getHighlighter("Json") shouldBe jsonHighlighter
        }

        @Test
        fun `should overwrite existing highlighter for same language`() {
            val first = createMockHighlighter("json")
            val second = createMockHighlighter("json")

            registry.register(first)
            registry.register(second)

            registry.getHighlighter("json") shouldBe second
        }
    }

    @Nested
    inner class GetHighlighterForContentType {

        @Test
        fun `should return highlighter for application json`() {
            val jsonHighlighter = createMockHighlighter("json")
            registry.register(jsonHighlighter)

            registry.getHighlighterForContentType("application/json") shouldBe jsonHighlighter
        }

        @Test
        fun `should return highlighter for text xml`() {
            val xmlHighlighter = createMockHighlighter("xml")
            registry.register(xmlHighlighter)

            registry.getHighlighterForContentType("text/xml") shouldBe xmlHighlighter
        }

        @Test
        fun `should return highlighter for application xml`() {
            val xmlHighlighter = createMockHighlighter("xml")
            registry.register(xmlHighlighter)

            registry.getHighlighterForContentType("application/xml") shouldBe xmlHighlighter
        }

        @Test
        fun `should strip charset and match content type`() {
            val jsonHighlighter = createMockHighlighter("json")
            registry.register(jsonHighlighter)

            registry.getHighlighterForContentType("application/json; charset=utf-8") shouldBe jsonHighlighter
        }

        @Test
        fun `should detect json from plus json suffix pattern`() {
            val jsonHighlighter = createMockHighlighter("json")
            registry.register(jsonHighlighter)

            registry.getHighlighterForContentType("application/vnd.api+json") shouldBe jsonHighlighter
        }

        @Test
        fun `should detect xml from plus xml suffix pattern`() {
            val xmlHighlighter = createMockHighlighter("xml")
            registry.register(xmlHighlighter)

            registry.getHighlighterForContentType("application/atom+xml") shouldBe xmlHighlighter
        }

        @Test
        fun `should return null for unknown content type`() {
            registry.getHighlighterForContentType("application/octet-stream") shouldBe null
        }

        @Test
        fun `should return html highlighter for text html`() {
            val htmlHighlighter = createMockHighlighter("html")
            registry.register(htmlHighlighter)

            registry.getHighlighterForContentType("text/html") shouldBe htmlHighlighter
        }
    }

    @Nested
    inner class GetAllHighlighters {

        @Test
        fun `should return all registered highlighters`() {
            val json = createMockHighlighter("json")
            val xml = createMockHighlighter("xml")

            registry.register(json)
            registry.register(xml)

            registry.getAllHighlighters() shouldHaveSize 2
        }

        @Test
        fun `should return empty list when none registered`() {
            registry.getAllHighlighters() shouldHaveSize 0
        }
    }

    @Nested
    inner class GetSupportedLanguages {

        @Test
        fun `should return all registered language identifiers`() {
            registry.register(createMockHighlighter("json"))
            registry.register(createMockHighlighter("xml"))
            registry.register(createMockHighlighter("html"))

            registry.getSupportedLanguages() shouldContainExactlyInAnyOrder listOf("json", "xml", "html")
        }
    }

    private fun createMockHighlighter(lang: String): SyntaxHighlighter {
        val highlighter = mockk<SyntaxHighlighter>()
        every { highlighter.language } returns lang
        return highlighter
    }
}
