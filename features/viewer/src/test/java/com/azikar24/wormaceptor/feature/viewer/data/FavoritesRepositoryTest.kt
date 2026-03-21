package com.azikar24.wormaceptor.feature.viewer.data

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import app.cash.turbine.test
import com.azikar24.wormaceptor.api.Feature
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FavoritesRepositoryTest {

    private val prefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkStatic(StrictMode::class)
        every { StrictMode.allowThreadDiskReads() } returns mockk(relaxed = true)
        every { StrictMode.setThreadPolicy(any()) } just Runs

        every { context.applicationContext } returns context
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putStringSet(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } just Runs
        every { editor.commit() } returns true

        resetSingleton()
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(StrictMode::class)
        resetSingleton()
    }

    private fun resetSingleton() {
        val field = FavoritesRepository::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    private fun createRepository(savedFavorites: Set<String>? = null): FavoritesRepository {
        every { prefs.getStringSet("favorite_tools", null) } returns savedFavorites
        return FavoritesRepository.getInstance(context)
    }

    @Nested
    inner class Initialization {

        @Test
        fun `should start with empty favorites when prefs have no saved data`() {
            val repo = createRepository(savedFavorites = null)

            repo.getFavoritesCount().shouldBeZero()
        }

        @Test
        fun `should load saved favorites from SharedPreferences`() {
            val repo = createRepository(
                savedFavorites = setOf("MEMORY_MONITOR", "FPS_MONITOR"),
            )

            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeTrue()
            repo.isFavorite(Feature.FPS_MONITOR).shouldBeTrue()
            repo.getFavoritesCount() shouldBe 2
        }

        @Test
        fun `should ignore invalid feature names in SharedPreferences`() {
            val repo = createRepository(
                savedFavorites = setOf("MEMORY_MONITOR", "INVALID_FEATURE"),
            )

            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeTrue()
            repo.getFavoritesCount() shouldBe 1
        }

        @Test
        fun `should suppress StrictMode disk read during init`() {
            createRepository()

            verify { StrictMode.allowThreadDiskReads() }
            verify { StrictMode.setThreadPolicy(any()) }
        }
    }

    @Nested
    inner class AddFavorite {

        @Test
        fun `should add feature to favorites`() {
            val repo = createRepository()

            val result = repo.addFavorite(Feature.MEMORY_MONITOR)

            result.shouldBeTrue()
            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeTrue()
        }

        @Test
        fun `should persist added favorite to SharedPreferences`() {
            val repo = createRepository()

            repo.addFavorite(Feature.MEMORY_MONITOR)

            verify { editor.putStringSet("favorite_tools", setOf("MEMORY_MONITOR")) }
        }

        @Test
        fun `should return true when adding duplicate feature`() {
            val repo = createRepository()
            repo.addFavorite(Feature.MEMORY_MONITOR)

            val result = repo.addFavorite(Feature.MEMORY_MONITOR)

            result.shouldBeTrue()
            repo.getFavoritesCount() shouldBe 1
        }

        @Test
        fun `should return false when at max capacity`() {
            val allFeatures = Feature.entries.take(FavoritesRepository.MAX_FAVORITES).toSet()
            val repo = createRepository(
                savedFavorites = allFeatures.map { it.name }.toSet(),
            )

            val newFeature = Feature.entries.drop(FavoritesRepository.MAX_FAVORITES).first()
            val result = repo.addFavorite(newFeature)

            result.shouldBeFalse()
        }

        @Test
        fun `should not exceed max favorites`() {
            val allFeatures = Feature.entries.take(FavoritesRepository.MAX_FAVORITES).toSet()
            val repo = createRepository(
                savedFavorites = allFeatures.map { it.name }.toSet(),
            )

            val newFeature = Feature.entries.drop(FavoritesRepository.MAX_FAVORITES).first()
            repo.addFavorite(newFeature)

            repo.getFavoritesCount() shouldBe FavoritesRepository.MAX_FAVORITES
            repo.isFavorite(newFeature).shouldBeFalse()
        }
    }

    @Nested
    inner class RemoveFavorite {

        @Test
        fun `should remove feature from favorites`() {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR"))

            repo.removeFavorite(Feature.MEMORY_MONITOR)

            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeFalse()
            repo.getFavoritesCount().shouldBeZero()
        }

        @Test
        fun `should persist removal to SharedPreferences`() {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR", "FPS_MONITOR"))

            repo.removeFavorite(Feature.MEMORY_MONITOR)

            verify { editor.putStringSet("favorite_tools", setOf("FPS_MONITOR")) }
        }

        @Test
        fun `should do nothing when removing feature that is not a favorite`() {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR"))

            repo.removeFavorite(Feature.FPS_MONITOR)

            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeTrue()
            repo.getFavoritesCount() shouldBe 1
        }
    }

    @Nested
    inner class ToggleFavorite {

        @Test
        fun `should add feature when not a favorite and return true`() {
            val repo = createRepository()

            val result = repo.toggleFavorite(Feature.MEMORY_MONITOR)

            result.shouldBeTrue()
            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeTrue()
        }

        @Test
        fun `should remove feature when already a favorite and return false`() {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR"))

            val result = repo.toggleFavorite(Feature.MEMORY_MONITOR)

            result.shouldBeFalse()
            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeFalse()
        }

        @Test
        fun `should return false when adding would exceed max capacity`() {
            val allFeatures = Feature.entries.take(FavoritesRepository.MAX_FAVORITES).toSet()
            val repo = createRepository(
                savedFavorites = allFeatures.map { it.name }.toSet(),
            )

            val newFeature = Feature.entries.drop(FavoritesRepository.MAX_FAVORITES).first()
            val result = repo.toggleFavorite(newFeature)

            result.shouldBeFalse()
        }
    }

    @Nested
    inner class IsFavorite {

        @Test
        fun `should return true for a favorite feature`() {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR"))

            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeTrue()
        }

        @Test
        fun `should return false for a non-favorite feature`() {
            val repo = createRepository()

            repo.isFavorite(Feature.MEMORY_MONITOR).shouldBeFalse()
        }
    }

    @Nested
    inner class ClearFavorites {

        @Test
        fun `should remove all favorites`() {
            val repo = createRepository(
                savedFavorites = setOf("MEMORY_MONITOR", "FPS_MONITOR"),
            )

            repo.clearFavorites()

            repo.getFavoritesCount().shouldBeZero()
        }

        @Test
        fun `should remove key from SharedPreferences`() {
            val repo = createRepository(
                savedFavorites = setOf("MEMORY_MONITOR"),
            )

            repo.clearFavorites()

            verify { editor.remove("favorite_tools") }
        }

        @Test
        fun `should handle clearing already empty favorites`() {
            val repo = createRepository()

            repo.clearFavorites()

            repo.getFavoritesCount().shouldBeZero()
        }
    }

    @Nested
    inner class SetDefaultsIfNeeded {

        @Test
        fun `should set defaults when no favorites key exists`() {
            val repo = createRepository()
            every { prefs.contains("favorite_tools") } returns false

            repo.setDefaultsIfNeeded()

            FavoritesRepository.DEFAULT_FAVORITES.forEach { feature ->
                repo.isFavorite(feature).shouldBeTrue()
            }
        }

        @Test
        fun `should not overwrite existing favorites`() {
            val repo = createRepository(savedFavorites = setOf("FPS_MONITOR"))
            every { prefs.contains("favorite_tools") } returns true

            repo.setDefaultsIfNeeded()

            repo.isFavorite(Feature.FPS_MONITOR).shouldBeTrue()
            repo.getFavoritesCount() shouldBe 1
        }

        @Test
        fun `should persist defaults to SharedPreferences`() {
            val repo = createRepository()
            every { prefs.contains("favorite_tools") } returns false

            repo.setDefaultsIfNeeded()

            val expectedNames = FavoritesRepository.DEFAULT_FAVORITES.map { it.name }.toSet()
            verify { editor.putStringSet("favorite_tools", expectedNames) }
        }
    }

    @Nested
    inner class StateFlowEmissions {

        @Test
        fun `should emit initial empty set`() = runTest {
            val repo = createRepository()

            repo.favorites.test {
                awaitItem().shouldBeEmpty()
            }
        }

        @Test
        fun `should emit updated set when favorite is added`() = runTest {
            val repo = createRepository()

            repo.favorites.test {
                awaitItem().shouldBeEmpty()
                repo.addFavorite(Feature.MEMORY_MONITOR)
                awaitItem() shouldContain Feature.MEMORY_MONITOR
            }
        }

        @Test
        fun `should emit updated set when favorite is removed`() = runTest {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR", "FPS_MONITOR"))

            repo.favorites.test {
                awaitItem().size shouldBe 2
                repo.removeFavorite(Feature.MEMORY_MONITOR)
                val updated = awaitItem()
                updated shouldNotContain Feature.MEMORY_MONITOR
                updated shouldContain Feature.FPS_MONITOR
            }
        }

        @Test
        fun `should emit empty set when favorites are cleared`() = runTest {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR"))

            repo.favorites.test {
                awaitItem().size shouldBe 1
                repo.clearFavorites()
                awaitItem().shouldBeEmpty()
            }
        }

        @Test
        fun `should emit defaults when setDefaultsIfNeeded is called`() = runTest {
            val repo = createRepository()
            every { prefs.contains("favorite_tools") } returns false

            repo.favorites.test {
                awaitItem().shouldBeEmpty()
                repo.setDefaultsIfNeeded()
                awaitItem() shouldContainExactlyInAnyOrder FavoritesRepository.DEFAULT_FAVORITES
            }
        }
    }

    @Nested
    inner class DuplicateHandling {

        @Test
        fun `should not increase count when adding same feature twice`() {
            val repo = createRepository()

            repo.addFavorite(Feature.MEMORY_MONITOR)
            repo.addFavorite(Feature.MEMORY_MONITOR)

            repo.getFavoritesCount() shouldBe 1
        }

        @Test
        fun `should not change state when removing non-existent feature`() = runTest {
            val repo = createRepository(savedFavorites = setOf("MEMORY_MONITOR"))

            repo.favorites.test {
                awaitItem().size shouldBe 1
                repo.removeFavorite(Feature.FPS_MONITOR)
                expectNoEvents()
            }
        }
    }

    @Nested
    inner class CompanionObject {

        @Test
        fun `MAX_FAVORITES should be 10`() {
            FavoritesRepository.MAX_FAVORITES shouldBe 10
        }

        @Test
        fun `DEFAULT_FAVORITES should contain expected features`() {
            FavoritesRepository.DEFAULT_FAVORITES shouldContainExactlyInAnyOrder setOf(
                Feature.CONSOLE_LOGS,
                Feature.SHARED_PREFERENCES,
                Feature.MEMORY_MONITOR,
            )
        }
    }
}
