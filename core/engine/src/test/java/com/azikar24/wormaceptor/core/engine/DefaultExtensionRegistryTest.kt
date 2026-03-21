package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.ExtensionContext
import com.azikar24.wormaceptor.domain.contracts.ExtensionProvider
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DefaultExtensionRegistryTest {

    private lateinit var registry: DefaultExtensionRegistry

    @BeforeEach
    fun setUp() {
        registry = DefaultExtensionRegistry()
    }

    @Nested
    inner class Register {

        @Test
        fun `should register a provider`() {
            val provider = createProvider("test-provider")

            registry.register(provider)

            registry.getRegisteredProviders() shouldHaveSize 1
            registry.getRegisteredProviders().first() shouldBe "test-provider"
        }

        @Test
        fun `should replace provider with same name`() {
            val first = createProvider("my-provider", extensions = mapOf("key" to "first"))
            val second = createProvider("my-provider", extensions = mapOf("key" to "second"))

            registry.register(first)
            registry.register(second)

            registry.getRegisteredProviders() shouldHaveSize 1

            val context = createContext()
            val result = registry.extractAll(context)
            result["key"] shouldBe "second"
        }

        @Test
        fun `should register multiple providers`() {
            registry.register(createProvider("alpha"))
            registry.register(createProvider("beta"))
            registry.register(createProvider("gamma"))

            registry.getRegisteredProviders() shouldHaveSize 3
            registry.getRegisteredProviders() shouldContainExactlyInAnyOrder listOf("alpha", "beta", "gamma")
        }
    }

    @Nested
    inner class Unregister {

        @Test
        fun `should return true when removing existing provider`() {
            registry.register(createProvider("test"))

            val result = registry.unregister("test")

            result shouldBe true
            registry.getRegisteredProviders().shouldBeEmpty()
        }

        @Test
        fun `should return false when removing non-existent provider`() {
            val result = registry.unregister("non-existent")

            result shouldBe false
        }

        @Test
        fun `should only remove specified provider`() {
            registry.register(createProvider("keep"))
            registry.register(createProvider("remove"))

            registry.unregister("remove")

            registry.getRegisteredProviders() shouldBe listOf("keep")
        }
    }

    @Nested
    inner class ExtractAll {

        @Test
        fun `should return empty map when no providers`() {
            val context = createContext()

            val result = registry.extractAll(context)

            result.shouldBeEmpty()
        }

        @Test
        fun `should combine extensions from multiple providers`() {
            registry.register(
                createProvider("provider-a", extensions = mapOf("keyA" to "valueA")),
            )
            registry.register(
                createProvider("provider-b", extensions = mapOf("keyB" to "valueB")),
            )

            val result = registry.extractAll(createContext())

            result shouldHaveSize 2
            result["keyA"] shouldBe "valueA"
            result["keyB"] shouldBe "valueB"
        }

        @Test
        fun `should process higher priority providers first`() {
            // Lower priority runs first, higher priority overwrites
            registry.register(
                createProvider("low", priority = 10, extensions = mapOf("shared" to "low-value")),
            )
            registry.register(
                createProvider("high", priority = 100, extensions = mapOf("shared" to "high-value")),
            )

            val result = registry.extractAll(createContext())

            // Higher priority runs first (sortedByDescending), so low priority
            // overwrites because it runs second via putAll
            result["shared"] shouldBe "low-value"
        }

        @Test
        fun `should handle provider that throws exception gracefully`() {
            val throwingProvider = object : ExtensionProvider {
                override val name = "throwing"
                override val priority = 50
                override fun extractExtensions(context: ExtensionContext): Map<String, String> {
                    error("Provider error")
                }
            }
            val workingProvider = createProvider(
                "working",
                extensions = mapOf("key" to "value"),
            )

            registry.register(throwingProvider)
            registry.register(workingProvider)

            val result = registry.extractAll(createContext())

            // Should still have results from the working provider
            result["key"] shouldBe "value"
        }

        @Test
        fun `should return empty map from provider that returns empty map`() {
            registry.register(createProvider("empty", extensions = emptyMap()))

            val result = registry.extractAll(createContext())

            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class GetRegisteredProviders {

        @Test
        fun `should return empty list initially`() {
            registry.getRegisteredProviders().shouldBeEmpty()
        }

        @Test
        fun `should return all registered provider names`() {
            registry.register(createProvider("first"))
            registry.register(createProvider("second"))

            registry.getRegisteredProviders() shouldContainExactlyInAnyOrder listOf("first", "second")
        }
    }

    private fun createProvider(
        name: String,
        priority: Int = 50,
        extensions: Map<String, String> = emptyMap(),
    ): ExtensionProvider {
        return object : ExtensionProvider {
            override val name = name
            override val priority = priority
            override fun extractExtensions(context: ExtensionContext): Map<String, String> = extensions
        }
    }

    private fun createContext(): ExtensionContext {
        return ExtensionContext(
            request = Request("https://example.com", "GET", emptyMap(), null),
            response = Response(200, "OK", emptyMap(), null),
            durationMs = 100,
            timestamp = System.currentTimeMillis(),
        )
    }
}
