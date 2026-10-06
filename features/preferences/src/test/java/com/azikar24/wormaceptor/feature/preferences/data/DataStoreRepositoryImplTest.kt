package com.azikar24.wormaceptor.feature.preferences.data

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceSource
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DataStoreRepositoryImplTest {

    private val fileName = "settings.preferences_pb"
    private val dataSource = mockk<DataStoreDataSource>(relaxed = true)
    private val repository = DataStoreRepositoryImpl(dataSource)

    @Test
    fun `lists files from the data source`() = runTest {
        val files = listOf(PreferenceFile(fileName, 2, PreferenceSource.DATASTORE))
        every { dataSource.getPreferenceFiles() } returns files

        repository.observePreferenceFiles().test {
            awaitItem() shouldBe files
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits decoded items for a file`() = runTest {
        val items = listOf(PreferenceItem("quota", PreferenceValue.DoubleValue(1.5)))
        every { dataSource.lastModified(fileName) } returns 1L
        every { dataSource.getPreferenceItems(fileName) } returns items

        repository.observePreferenceItems(fileName).test {
            awaitItem() shouldBe items
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `looks up a single key`() = runTest {
        every { dataSource.getPreferenceItems(fileName) } returns listOf(
            PreferenceItem("quota", PreferenceValue.DoubleValue(1.5)),
        )

        repository.getPreference(fileName, "quota") shouldBe PreferenceValue.DoubleValue(1.5)
        repository.getPreference(fileName, "missing").shouldBeNull()
    }
}
