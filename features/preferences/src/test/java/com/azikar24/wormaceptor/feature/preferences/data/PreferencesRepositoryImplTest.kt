package com.azikar24.wormaceptor.feature.preferences.data

import android.content.SharedPreferences
import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dataSource = mockk<PreferencesDataSource>(relaxed = true)
    private lateinit var repository: PreferencesRepositoryImpl

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = PreferencesRepositoryImpl(dataSource)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `observePreferenceFiles` {

        @Test
        fun `emits preference files from data source`() = runTest {
            val files = listOf(
                PreferenceFile("settings", 3),
                PreferenceFile("cache", 1),
            )
            every { dataSource.getPreferenceFiles() } returns files

            repository.observePreferenceFiles().test {
                val result = awaitItem()
                result shouldHaveSize 2
                result.first().name shouldBe "settings"
                result.first().itemCount shouldBe 3
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `observePreferenceItems` {

        @Test
        fun `emits initial items from data source`() = runTest {
            val items = listOf(
                PreferenceItem("key1", PreferenceValue.StringValue("val1")),
                PreferenceItem("key2", PreferenceValue.IntValue(42)),
            )
            every { dataSource.getPreferenceItems("file") } returns items

            val prefs = mockk<SharedPreferences>(relaxed = true)
            every { dataSource.registerChangeListener("file", any()) } returns prefs

            repository.observePreferenceItems("file").test {
                val result = awaitItem()
                result shouldHaveSize 2
                result[0].key shouldBe "key1"
                result[1].key shouldBe "key2"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `unregisters listener on cancellation`() = runTest {
            every { dataSource.getPreferenceItems("file") } returns emptyList()

            val prefs = mockk<SharedPreferences>(relaxed = true)
            val listenerSlot = slot<SharedPreferences.OnSharedPreferenceChangeListener>()
            every { dataSource.registerChangeListener("file", capture(listenerSlot)) } returns prefs

            repository.observePreferenceItems("file").test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            verify { dataSource.unregisterChangeListener(prefs, any()) }
        }
    }

    @Nested
    inner class `getPreference` {

        @Test
        fun `delegates to data source and returns value`() = runTest {
            every { dataSource.getPreference("file", "key") } returns PreferenceValue.BooleanValue(true)

            val result = repository.getPreference("file", "key")

            result.shouldBeInstanceOf<PreferenceValue.BooleanValue>()
            (result as PreferenceValue.BooleanValue).value shouldBe true
        }

        @Test
        fun `returns null when data source returns null`() = runTest {
            every { dataSource.getPreference("file", "missing") } returns null

            val result = repository.getPreference("file", "missing")

            result.shouldBeNull()
        }
    }

    @Nested
    inner class `setPreference` {

        @Test
        fun `delegates to data source`() = runTest {
            val value = PreferenceValue.StringValue("new_value")

            repository.setPreference("file", "key", value)

            verify { dataSource.setPreference("file", "key", value) }
        }
    }

    @Nested
    inner class `deletePreference` {

        @Test
        fun `delegates to data source`() = runTest {
            repository.deletePreference("file", "key")

            verify { dataSource.deletePreference("file", "key") }
        }
    }

    @Nested
    inner class `clearFile` {

        @Test
        fun `delegates to data source`() = runTest {
            repository.clearFile("file")

            verify { dataSource.clearFile("file") }
        }
    }
}
