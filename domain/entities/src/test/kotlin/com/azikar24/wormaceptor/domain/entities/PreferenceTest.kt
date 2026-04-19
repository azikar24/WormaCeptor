package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PreferenceValueTest {

    @Nested
    inner class StringValueSubtype {

        @Test
        fun `stores value and provides displayValue`() {
            val pv = PreferenceValue.StringValue("hello")

            pv.value shouldBe "hello"
            pv.displayValue shouldBe "hello"
            pv.typeName shouldBe "String"
        }
    }

    @Nested
    inner class IntValueSubtype {

        @Test
        fun `stores value and formats displayValue`() {
            val pv = PreferenceValue.IntValue(42)

            pv.value shouldBe 42
            pv.displayValue shouldBe "42"
            pv.typeName shouldBe "Int"
        }
    }

    @Nested
    inner class LongValueSubtype {

        @Test
        fun `stores value and formats displayValue`() {
            val pv = PreferenceValue.LongValue(1_000_000L)

            pv.value shouldBe 1_000_000L
            pv.displayValue shouldBe "1000000"
            pv.typeName shouldBe "Long"
        }
    }

    @Nested
    inner class FloatValueSubtype {

        @Test
        fun `stores value and formats displayValue`() {
            val pv = PreferenceValue.FloatValue(3.14f)

            pv.value shouldBe 3.14f
            pv.displayValue shouldBe "3.14"
            pv.typeName shouldBe "Float"
        }
    }

    @Nested
    inner class BooleanValueSubtype {

        @Test
        fun `stores true value`() {
            val pv = PreferenceValue.BooleanValue(true)

            pv.value shouldBe true
            pv.displayValue shouldBe "true"
            pv.typeName shouldBe "Boolean"
        }

        @Test
        fun `stores false value`() {
            val pv = PreferenceValue.BooleanValue(false)

            pv.value shouldBe false
            pv.displayValue shouldBe "false"
        }
    }

    @Nested
    inner class StringSetValueSubtype {

        @Test
        fun `stores set and provides comma-separated displayValue`() {
            val pv = PreferenceValue.StringSetValue(setOf("a", "b", "c"))

            pv.value shouldBe setOf("a", "b", "c")
            pv.typeName shouldBe "StringSet"
            // displayValue joins with ", "
            (pv.displayValue.contains("a")) shouldBe true
            (pv.displayValue.contains("b")) shouldBe true
            (pv.displayValue.contains("c")) shouldBe true
        }

        @Test
        fun `empty set produces empty displayValue`() {
            val pv = PreferenceValue.StringSetValue(emptySet())

            pv.displayValue shouldBe ""
        }
    }

    @Nested
    inner class FromAnyFactory {

        @Test
        fun `converts String to StringValue`() {
            val result = PreferenceValue.fromAny("hello")

            result.shouldBeInstanceOf<PreferenceValue.StringValue>()
            result.value shouldBe "hello"
        }

        @Test
        fun `converts Int to IntValue`() {
            val result = PreferenceValue.fromAny(42)

            result.shouldBeInstanceOf<PreferenceValue.IntValue>()
            result.value shouldBe 42
        }

        @Test
        fun `converts Long to LongValue`() {
            val result = PreferenceValue.fromAny(100L)

            result.shouldBeInstanceOf<PreferenceValue.LongValue>()
            result.value shouldBe 100L
        }

        @Test
        fun `converts Float to FloatValue`() {
            val result = PreferenceValue.fromAny(1.5f)

            result.shouldBeInstanceOf<PreferenceValue.FloatValue>()
            result.value shouldBe 1.5f
        }

        @Test
        fun `converts Boolean to BooleanValue`() {
            val result = PreferenceValue.fromAny(true)

            result.shouldBeInstanceOf<PreferenceValue.BooleanValue>()
            result.value shouldBe true
        }

        @Test
        fun `converts Set of String to StringSetValue`() {
            val result = PreferenceValue.fromAny(setOf("x", "y"))

            result.shouldBeInstanceOf<PreferenceValue.StringSetValue>()
            result.value shouldBe setOf("x", "y")
        }

        @Test
        fun `returns null for null input`() {
            val result = PreferenceValue.fromAny(null)

            result shouldBe null
        }

        @Test
        fun `returns null for unsupported types`() {
            val result = PreferenceValue.fromAny(listOf("a", "b"))

            result shouldBe null
        }

        @Test
        fun `returns null for mixed type sets`() {
            @Suppress("UNCHECKED_CAST")
            val mixedSet = setOf("a", 1) as Set<Any>
            val result = PreferenceValue.fromAny(mixedSet)

            result shouldBe null
        }
    }

    @Nested
    inner class SealedClassExhaustiveness {

        @Test
        fun `exhaustive when covers all subtypes`() {
            val values = listOf(
                PreferenceValue.StringValue("s"),
                PreferenceValue.IntValue(1),
                PreferenceValue.LongValue(1L),
                PreferenceValue.FloatValue(1f),
                PreferenceValue.BooleanValue(true),
                PreferenceValue.StringSetValue(emptySet()),
            )

            val typeNames = values.map { pv ->
                when (pv) {
                    is PreferenceValue.StringValue -> "String"
                    is PreferenceValue.IntValue -> "Int"
                    is PreferenceValue.LongValue -> "Long"
                    is PreferenceValue.FloatValue -> "Float"
                    is PreferenceValue.BooleanValue -> "Boolean"
                    is PreferenceValue.StringSetValue -> "StringSet"
                }
            }

            typeNames shouldBe listOf("String", "Int", "Long", "Float", "Boolean", "StringSet")
        }
    }
}

class PreferenceItemTest {

    @Nested
    inner class Construction {

        @Test
        fun `stores key and value`() {
            val item = PreferenceItem(
                key = "dark_mode",
                value = PreferenceValue.BooleanValue(true),
            )

            item.key shouldBe "dark_mode"
            item.value.shouldBeInstanceOf<PreferenceValue.BooleanValue>()
        }
    }

    @Nested
    inner class EqualityAndCopy {

        @Test
        fun `equal instances are equal`() {
            val i1 = PreferenceItem("k", PreferenceValue.IntValue(1))
            val i2 = PreferenceItem("k", PreferenceValue.IntValue(1))

            i1 shouldBe i2
            i1.hashCode() shouldBe i2.hashCode()
        }

        @Test
        fun `different key makes instances unequal`() {
            val i1 = PreferenceItem("k1", PreferenceValue.IntValue(1))
            val i2 = PreferenceItem("k2", PreferenceValue.IntValue(1))

            i1 shouldNotBe i2
        }
    }
}

class PreferenceFileTest {

    @Nested
    inner class Construction {

        @Test
        fun `stores name and itemCount`() {
            val file = PreferenceFile(name = "app_prefs", itemCount = 15)

            file.name shouldBe "app_prefs"
            file.itemCount shouldBe 15
        }
    }

    @Nested
    inner class EqualityAndCopy {

        @Test
        fun `equal instances are equal`() {
            PreferenceFile("a", 1) shouldBe PreferenceFile("a", 1)
        }

        @Test
        fun `different name makes instances unequal`() {
            PreferenceFile("a", 1) shouldNotBe PreferenceFile("b", 1)
        }
    }
}
