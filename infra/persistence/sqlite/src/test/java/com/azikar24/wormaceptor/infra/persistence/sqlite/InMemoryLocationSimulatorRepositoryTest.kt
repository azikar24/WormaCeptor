package com.azikar24.wormaceptor.infra.persistence.sqlite

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryLocationSimulatorRepositoryTest {

    private val repository = InMemoryLocationSimulatorRepository()

    private fun createPreset(
        id: String = "custom_1",
        name: String = "Custom Location",
        latitude: Double = 25.0,
        longitude: Double = 45.0,
        isBuiltIn: Boolean = false,
    ) = LocationPreset(
        id = id,
        name = name,
        location = MockLocation.from(latitude, longitude, name),
        isBuiltIn = isBuiltIn,
    )

    @Nested
    inner class `getPresets` {

        @Test
        fun `returns built-in presets initially`() = runTest {
            repository.getPresets().test {
                val presets = awaitItem()
                presets shouldHaveSize 7
                presets.map { it.name } shouldBe listOf(
                    "Kuwait City",
                    "Cairo",
                    "New York",
                    "London",
                    "Tokyo",
                    "Sydney",
                    "Paris",
                )
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `built-in presets have isBuiltIn set to true`() = runTest {
            repository.getPresets().test {
                val presets = awaitItem()
                presets.forEach { it.isBuiltIn shouldBe true }
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `built-in presets have correct coordinates`() = runTest {
            repository.getPresets().test {
                val presets = awaitItem()
                val kuwait = presets.first { it.name == "Kuwait City" }
                kuwait.location.latitude shouldBe 29.3759
                kuwait.location.longitude shouldBe 47.9774
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `savePreset` {

        @Test
        fun `adds a custom preset to the list`() = runTest {
            val custom = createPreset(id = "custom_test", name = "Test City")

            repository.savePreset(custom)

            repository.getPresets().test {
                val presets = awaitItem()
                presets shouldHaveSize 8
                val added = presets.first { it.id == "custom_test" }
                added.name shouldBe "Test City"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `marks saved preset as not built-in regardless of input`() = runTest {
            val preset = createPreset(isBuiltIn = true)

            repository.savePreset(preset)

            repository.getPresets().test {
                val saved = awaitItem().first { it.id == "custom_1" }
                saved.isBuiltIn shouldBe false
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `replaces existing preset with same ID`() = runTest {
            val original = createPreset(id = "my_preset", name = "Original")
            val updated = createPreset(id = "my_preset", name = "Updated")

            repository.savePreset(original)
            repository.savePreset(updated)

            repository.getPresets().test {
                val presets = awaitItem()
                val matchingPresets = presets.filter { it.id == "my_preset" }
                matchingPresets shouldHaveSize 1
                matchingPresets.first().name shouldBe "Updated"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `preserves location details`() = runTest {
            val location = MockLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                altitude = 100.0,
                accuracy = 5.0f,
                speed = 10.0f,
                bearing = 180.0f,
                name = "NYC",
            )
            val preset = LocationPreset(
                id = "detailed",
                name = "Detailed Location",
                location = location,
            )

            repository.savePreset(preset)

            repository.getPresets().test {
                val saved = awaitItem().first { it.id == "detailed" }
                saved.location.latitude shouldBe 40.7128
                saved.location.longitude shouldBe -74.0060
                saved.location.name shouldBe "NYC"
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `deletePreset` {

        @Test
        fun `removes a custom preset`() = runTest {
            val custom = createPreset(id = "to_delete", name = "Delete Me")
            repository.savePreset(custom)

            repository.deletePreset("to_delete")

            repository.getPresets().test {
                val presets = awaitItem()
                presets.none { it.id == "to_delete" } shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `does not delete built-in presets`() = runTest {
            repository.deletePreset("builtin_kuwait")

            repository.getPresets().test {
                val presets = awaitItem()
                presets.any { it.id == "builtin_kuwait" } shouldBe true
                presets shouldHaveSize 7
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `does not throw for non-existent preset ID`() = runTest {
            repository.deletePreset("non_existent_id")

            repository.getPresets().test {
                awaitItem() shouldHaveSize 7
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `does not affect other custom presets`() = runTest {
            repository.savePreset(createPreset(id = "keep_this", name = "Keep"))
            repository.savePreset(createPreset(id = "remove_this", name = "Remove"))

            repository.deletePreset("remove_this")

            repository.getPresets().test {
                val presets = awaitItem()
                presets.any { it.id == "keep_this" } shouldBe true
                presets.none { it.id == "remove_this" } shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `getCurrentMockLocation` {

        @Test
        fun `returns null initially`() = runTest {
            repository.getCurrentMockLocation().test {
                awaitItem().shouldBeNull()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `returns the set mock location`() = runTest {
            val location = MockLocation.from(35.0, 139.0, "Tokyo")

            repository.setMockLocation(location)

            repository.getCurrentMockLocation().test {
                val result = awaitItem()
                result.shouldNotBeNull()
                result.latitude shouldBe 35.0
                result.longitude shouldBe 139.0
                result.name shouldBe "Tokyo"
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `setMockLocation` {

        @Test
        fun `sets a mock location`() = runTest {
            val location = MockLocation.from(48.8566, 2.3522, "Paris")

            repository.setMockLocation(location)

            repository.getCurrentMockLocation().test {
                val result = awaitItem()
                result.shouldNotBeNull()
                result.latitude shouldBe 48.8566
                result.longitude shouldBe 2.3522
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `can clear mock location by setting null`() = runTest {
            repository.setMockLocation(MockLocation.from(10.0, 20.0))
            repository.setMockLocation(null)

            repository.getCurrentMockLocation().test {
                awaitItem().shouldBeNull()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `replaces previous mock location`() = runTest {
            repository.setMockLocation(MockLocation.from(10.0, 20.0, "First"))
            repository.setMockLocation(MockLocation.from(30.0, 40.0, "Second"))

            repository.getCurrentMockLocation().test {
                val result = awaitItem()
                result.shouldNotBeNull()
                result.name shouldBe "Second"
                result.latitude shouldBe 30.0
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}
