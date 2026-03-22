package com.azikar24.wormaceptor.feature.location.vm

import android.app.Application
import android.util.Log
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val presetsFlow = MutableStateFlow<List<LocationPreset>>(emptyList())
    private val currentMockLocationFlow = MutableStateFlow<MockLocation?>(null)
    private val engineMockLocationFlow = MutableStateFlow<MockLocation?>(null)
    private val isEnabledFlow = MutableStateFlow(false)
    private val lastErrorFlow = MutableStateFlow<String?>(null)

    private val repository = mockk<LocationSimulatorRepository>(relaxed = true) {
        every { getPresets() } returns presetsFlow
        every { getCurrentMockLocation() } returns currentMockLocationFlow
    }

    private val engine = mockk<LocationSimulatorEngine>(relaxed = true) {
        every { isMockLocationAvailable() } returns true
        every { currentMockLocation } returns engineMockLocationFlow
        every { isEnabled } returns isEnabledFlow
        every { lastError } returns lastErrorFlow
    }

    private val fusedLocationClient = mockk<FusedLocationProviderClient>(relaxed = true)
    private val context = mockk<Application>(relaxed = true)

    private lateinit var viewModel: LocationViewModel

    private val samplePreset = LocationPreset(
        id = "preset_1",
        name = "New York",
        location = MockLocation.from(40.7128, -74.0060, "New York"),
        isBuiltIn = true,
    )

    private val samplePreset2 = LocationPreset(
        id = "preset_2",
        name = "London",
        location = MockLocation.from(51.5074, -0.1278, "London"),
        isBuiltIn = false,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(LocationServices::class)
        mockkStatic(Log::class)
        every { LocationServices.getFusedLocationProviderClient(any<Application>()) } returns fusedLocationClient
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(LocationServices::class)
        unmockkStatic(Log::class)
    }

    private fun createViewModel(): LocationViewModel {
        return LocationViewModel(repository, engine, context)
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `latitudeInput is empty`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.latitudeInput shouldBe ""
        }

        @Test
        fun `longitudeInput is empty`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.longitudeInput shouldBe ""
        }

        @Test
        fun `searchQuery is empty`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.searchQuery shouldBe ""
        }

        @Test
        fun `isLoading is false`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.isLoading shouldBe false
        }

        @Test
        fun `isMockLocationAvailable reflects engine`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.isMockLocationAvailable shouldBe true
        }

        @Test
        fun `isMockLocationAvailable is false when engine says so`() = runTest {
            every { engine.isMockLocationAvailable() } returns false
            viewModel = createViewModel()
            viewModel.uiState.value.isMockLocationAvailable shouldBe false
        }

        @Test
        fun `isMapExpanded is false`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.isMapExpanded shouldBe false
        }

        @Test
        fun `isInputValid is false for empty inputs`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false
            }
        }
    }

    @Nested
    inner class `LatitudeChanged event` {

        @Test
        fun `updates latitudeInput in state`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))

            viewModel.uiState.value.latitudeInput shouldBe "40.7128"
        }
    }

    @Nested
    inner class `LongitudeChanged event` {

        @Test
        fun `updates longitudeInput in state`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))

            viewModel.uiState.value.longitudeInput shouldBe "-74.0060"
        }
    }

    @Nested
    inner class `SearchQueryChanged event` {

        @Test
        fun `updates searchQuery in state`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.SearchQueryChanged("new york"))

            viewModel.uiState.value.searchQuery shouldBe "new york"
        }
    }

    @Nested
    inner class `input validation` {

        @Test
        fun `valid coordinates produce true`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))

                awaitUntil { it } shouldBe true
            }
        }

        @Test
        fun `latitude out of range produces false`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("91.0"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("10.0"))
                advanceUntilIdle()

                expectNoEvents()
            }
        }

        @Test
        fun `longitude out of range produces false`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.0"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("181.0"))
                advanceUntilIdle()

                expectNoEvents()
            }
        }

        @Test
        fun `non-numeric latitude produces false`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("abc"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("10.0"))
                advanceUntilIdle()

                expectNoEvents()
            }
        }

        @Test
        fun `non-numeric longitude produces false`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.0"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("xyz"))
                advanceUntilIdle()

                expectNoEvents()
            }
        }

        @Test
        fun `empty strings produce false`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false
            }
        }

        @Test
        fun `boundary latitude 90 is valid`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("90.0"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("0.0"))

                awaitUntil { it } shouldBe true
            }
        }

        @Test
        fun `boundary latitude -90 is valid`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("-90.0"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("0.0"))

                awaitUntil { it } shouldBe true
            }
        }

        @Test
        fun `boundary longitude 180 is valid`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("0.0"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("180.0"))

                awaitUntil { it } shouldBe true
            }
        }

        @Test
        fun `boundary longitude -180 is valid`() = runTest {
            viewModel = createViewModel()
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("0.0"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-180.0"))

                awaitUntil { it } shouldBe true
            }
        }
    }

    @Nested
    inner class `SetMockLocationFromInput event` {

        @Test
        fun `sets mock location when input is valid`() = runTest {
            every { engine.setLocation(any()) } returns true
            viewModel = createViewModel()

            // Must actively subscribe to isInputValid so the stateIn flow starts
            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))
                awaitUntil { it } shouldBe true

                viewModel.effects.test {
                    viewModel.sendEvent(LocationViewEvent.SetMockLocationFromInput)
                    advanceUntilIdle()

                    val effect = awaitItem()
                    effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                }

                verify { engine.setLocation(match { it.latitude == 40.7128 && it.longitude == -74.006 }) }
            }
        }

        @Test
        fun `falls back to last mock location when input is invalid`() = runTest {
            val lastMock = MockLocation.from(51.5074, -0.1278)
            currentMockLocationFlow.value = lastMock
            every { engine.setLocation(any()) } returns true
            viewModel = createViewModel()

            // Must subscribe to currentMockLocation so the stateIn flow starts
            viewModel.currentMockLocation.test {
                awaitUntil { it != null }

                viewModel.effects.test {
                    viewModel.sendEvent(LocationViewEvent.SetMockLocationFromInput)
                    advanceUntilIdle()

                    val effect = awaitItem()
                    effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                }

                viewModel.uiState.value.latitudeInput shouldBe "%.6f".format(lastMock.latitude)
                viewModel.uiState.value.longitudeInput shouldBe "%.6f".format(lastMock.longitude)
            }
        }

        @Test
        fun `falls back to first preset when no input and no last mock`() = runTest {
            presetsFlow.value = listOf(samplePreset)
            every { engine.setLocation(any()) } returns true
            viewModel = createViewModel()

            // Subscribe to presets so the stateIn flow starts populating
            viewModel.presets.test {
                awaitUntil { it.isNotEmpty() }

                viewModel.effects.test {
                    viewModel.sendEvent(LocationViewEvent.SetMockLocationFromInput)
                    advanceUntilIdle()

                    val effect = awaitItem()
                    effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                }

                viewModel.uiState.value.latitudeInput shouldBe samplePreset.location.latitude.toString()
                viewModel.uiState.value.longitudeInput shouldBe samplePreset.location.longitude.toString()
            }
        }

        @Test
        fun `emits error when no input and no fallback available`() = runTest {
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SetMockLocationFromInput)

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe "Please enter valid coordinates"
            }
        }

        @Test
        fun `emits error when engine fails to set location`() = runTest {
            every { engine.setLocation(any()) } returns false
            lastErrorFlow.value = "Mock location not enabled"
            viewModel = createViewModel()

            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))
                awaitUntil { it } shouldBe true

                viewModel.effects.test {
                    viewModel.sendEvent(LocationViewEvent.SetMockLocationFromInput)
                    advanceUntilIdle()

                    val effect = awaitItem()
                    effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                    (effect as LocationViewEffect.ShowError).message shouldBe "Mock location not enabled"
                }
            }
        }

        @Test
        fun `emits generic error when engine fails with no lastError`() = runTest {
            every { engine.setLocation(any()) } returns false
            lastErrorFlow.value = null
            viewModel = createViewModel()

            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))
                awaitUntil { it } shouldBe true

                viewModel.effects.test {
                    viewModel.sendEvent(LocationViewEvent.SetMockLocationFromInput)
                    advanceUntilIdle()

                    val effect = awaitItem()
                    effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                    (effect as LocationViewEffect.ShowError).message shouldBe "Failed to set mock location"
                }
            }
        }

        @Test
        fun `sets isLoading during operation`() = runTest {
            every { engine.setLocation(any()) } returns true
            viewModel = createViewModel()

            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))
                awaitUntil { it } shouldBe true

                viewModel.sendEvent(LocationViewEvent.SetMockLocationFromInput)
                advanceUntilIdle()

                viewModel.uiState.value.isLoading shouldBe false
            }
        }
    }

    @Nested
    inner class `SetMockLocation event` {

        @Test
        fun `sets location via engine and repository`() = runTest {
            every { engine.setLocation(any()) } returns true
            viewModel = createViewModel()
            val location = MockLocation.from(40.7128, -74.0060)

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SetMockLocation(location))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
            }

            verify { engine.setLocation(location) }
            coVerify { repository.setMockLocation(location) }
        }

        @Test
        fun `emits error when engine throws exception`() = runTest {
            every { engine.setLocation(any()) } throws RuntimeException("Provider error")
            viewModel = createViewModel()
            val location = MockLocation.from(40.7128, -74.0060)

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SetMockLocation(location))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe
                    "Failed to set mock location: Provider error"
            }
        }
    }

    @Nested
    inner class `SetMockLocationFromPreset event` {

        @Test
        fun `sets location from preset and updates input fields`() = runTest {
            every { engine.setLocation(any()) } returns true
            viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SetMockLocationFromPreset(samplePreset))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
            }

            val state = viewModel.uiState.value
            state.latitudeInput shouldBe samplePreset.location.latitude.toString()
            state.longitudeInput shouldBe samplePreset.location.longitude.toString()
        }
    }

    @Nested
    inner class `ClearMockLocation event` {

        @Test
        fun `clears via engine and repository`() = runTest {
            viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.ClearMockLocation)
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                (effect as LocationViewEffect.ShowSuccess).message shouldBe "Mock location cleared"
            }

            verify { engine.clearMockLocation() }
            coVerify { repository.setMockLocation(null) }
        }

        @Test
        fun `emits error when engine throws exception`() = runTest {
            every { engine.clearMockLocation() } throws RuntimeException("Clear failed")
            viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.ClearMockLocation)
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe
                    "Failed to clear mock location: Clear failed"
            }
        }
    }

    @Nested
    inner class `ToggleMockLocation event` {

        @Test
        fun `clears mock when currently enabled`() = runTest {
            isEnabledFlow.value = true
            viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.ToggleMockLocation)
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                (effect as LocationViewEffect.ShowSuccess).message shouldBe "Mock location cleared"
            }

            verify { engine.clearMockLocation() }
        }

        @Test
        fun `sets mock from input when currently disabled`() = runTest {
            isEnabledFlow.value = false
            every { engine.setLocation(any()) } returns true
            viewModel = createViewModel()

            viewModel.isInputValid.test {
                awaitItem() shouldBe false

                viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
                viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))
                awaitUntil { it } shouldBe true

                viewModel.effects.test {
                    viewModel.sendEvent(LocationViewEvent.ToggleMockLocation)
                    advanceUntilIdle()

                    val effect = awaitItem()
                    effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                }
            }
        }
    }

    @Nested
    inner class `SaveCurrentAsPreset event` {

        @Test
        fun `saves preset when input is valid`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
            viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SaveCurrentAsPreset("My Location"))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                (effect as LocationViewEffect.ShowSuccess).message shouldBe "Preset saved: My Location"
            }

            coVerify {
                repository.savePreset(
                    match {
                        it.name == "My Location" &&
                            it.location.latitude == 40.7128 &&
                            it.location.longitude == -74.006 &&
                            !it.isBuiltIn
                    },
                )
            }
        }

        @Test
        fun `trims preset name`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
            viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SaveCurrentAsPreset("  My Location  "))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                (effect as LocationViewEffect.ShowSuccess).message shouldBe "Preset saved: My Location"
            }

            coVerify { repository.savePreset(match { it.name == "My Location" }) }
        }

        @Test
        fun `emits error when coordinates are invalid`() = runTest {
            viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SaveCurrentAsPreset("My Location"))

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe
                    "Please enter valid coordinates first"
            }
        }

        @Test
        fun `emits error when name is blank`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
            viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SaveCurrentAsPreset(""))

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe "Please enter a preset name"
            }
        }

        @Test
        fun `emits error when name is whitespace only`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
            viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SaveCurrentAsPreset("   "))

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe "Please enter a preset name"
            }
        }

        @Test
        fun `emits error when repository throws`() = runTest {
            coEvery { repository.savePreset(any()) } throws RuntimeException("DB error")
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.LatitudeChanged("40.7128"))
            viewModel.sendEvent(LocationViewEvent.LongitudeChanged("-74.0060"))

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.SaveCurrentAsPreset("My Place"))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe
                    "Failed to save preset: DB error"
            }
        }
    }

    @Nested
    inner class `DeletePreset event` {

        @Test
        fun `deletes preset via repository`() = runTest {
            viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.DeletePreset("preset_2"))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowSuccess>()
                (effect as LocationViewEffect.ShowSuccess).message shouldBe "Preset deleted"
            }

            coVerify { repository.deletePreset("preset_2") }
        }

        @Test
        fun `emits error when repository throws`() = runTest {
            coEvery { repository.deletePreset(any()) } throws RuntimeException("Delete failed")
            viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.sendEvent(LocationViewEvent.DeletePreset("preset_2"))
                advanceUntilIdle()

                val effect = awaitItem()
                effect.shouldBeInstanceOf<LocationViewEffect.ShowError>()
                (effect as LocationViewEffect.ShowError).message shouldBe
                    "Failed to delete preset: Delete failed"
            }
        }
    }

    @Nested
    inner class `MapTapped event` {

        @Test
        fun `updates lat and lng input from map tap`() = runTest {
            viewModel = createViewModel()

            viewModel.sendEvent(LocationViewEvent.MapTapped(48.8566, 2.3522))

            viewModel.uiState.value.latitudeInput shouldBe "%.6f".format(48.8566)
            viewModel.uiState.value.longitudeInput shouldBe "%.6f".format(2.3522)
        }
    }

    @Nested
    inner class `ToggleMapExpanded event` {

        @Test
        fun `toggles isMapExpanded from false to true`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.isMapExpanded shouldBe false

            viewModel.sendEvent(LocationViewEvent.ToggleMapExpanded)

            viewModel.uiState.value.isMapExpanded shouldBe true
        }

        @Test
        fun `toggles isMapExpanded from true to false`() = runTest {
            viewModel = createViewModel()
            viewModel.sendEvent(LocationViewEvent.ToggleMapExpanded)
            viewModel.uiState.value.isMapExpanded shouldBe true

            viewModel.sendEvent(LocationViewEvent.ToggleMapExpanded)

            viewModel.uiState.value.isMapExpanded shouldBe false
        }
    }

    @Nested
    inner class `RefreshMockLocationAvailability event` {

        @Test
        fun `updates isMockLocationAvailable from engine`() = runTest {
            viewModel = createViewModel()
            viewModel.uiState.value.isMockLocationAvailable shouldBe true

            every { engine.isMockLocationAvailable() } returns false
            viewModel.sendEvent(LocationViewEvent.RefreshMockLocationAvailability)

            viewModel.uiState.value.isMockLocationAvailable shouldBe false
        }
    }

    @Nested
    inner class `preset search filtering` {

        @Test
        fun `filters presets by name`() = runTest {
            presetsFlow.value = listOf(samplePreset, samplePreset2)
            viewModel = createViewModel()

            viewModel.presets.test {
                awaitUntil { it.size == 2 }

                viewModel.sendEvent(LocationViewEvent.SearchQueryChanged("New York"))

                val filtered = awaitUntil { it.size == 1 }
                filtered.first().name shouldBe "New York"
            }
        }

        @Test
        fun `filters presets by location name`() = runTest {
            presetsFlow.value = listOf(samplePreset, samplePreset2)
            viewModel = createViewModel()

            viewModel.presets.test {
                awaitUntil { it.size == 2 }

                viewModel.sendEvent(LocationViewEvent.SearchQueryChanged("London"))

                val filtered = awaitUntil { it.size == 1 }
                filtered.first().name shouldBe "London"
            }
        }

        @Test
        fun `blank search shows all presets`() = runTest {
            presetsFlow.value = listOf(samplePreset, samplePreset2)
            viewModel = createViewModel()

            viewModel.presets.test {
                awaitUntil { it.size == 2 }

                viewModel.sendEvent(LocationViewEvent.SearchQueryChanged("filter"))
                awaitUntil { it.isEmpty() }

                viewModel.sendEvent(LocationViewEvent.SearchQueryChanged(""))
                val all = awaitUntil { it.size == 2 }
                all.size shouldBe 2
            }
        }

        @Test
        fun `case insensitive search`() = runTest {
            presetsFlow.value = listOf(samplePreset, samplePreset2)
            viewModel = createViewModel()

            viewModel.presets.test {
                awaitUntil { it.size == 2 }

                viewModel.sendEvent(LocationViewEvent.SearchQueryChanged("new york"))

                val filtered = awaitUntil { it.size == 1 }
                filtered.first().name shouldBe "New York"
            }
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
