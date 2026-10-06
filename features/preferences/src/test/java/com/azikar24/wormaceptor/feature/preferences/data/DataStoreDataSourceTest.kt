package com.azikar24.wormaceptor.feature.preferences.data

import android.util.Log
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceSource
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DataStoreDataSourceTest {

    @TempDir
    lateinit var dir: File

    private val dataSource by lazy { DataStoreDataSource { dir } }

    private suspend fun writeWithDataStore(fileName: String) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        try {
            val store = PreferenceDataStoreFactory.create(scope = scope) { File(dir, fileName) }
            store.edit {
                it[booleanPreferencesKey("bool")] = true
                it[floatPreferencesKey("float")] = 1.5f
                it[intPreferencesKey("int")] = -42
                it[longPreferencesKey("long")] = Long.MAX_VALUE
                it[stringPreferencesKey("string")] = "héllo"
                it[stringSetPreferencesKey("set")] = setOf("a", "b")
                it[doublePreferencesKey("double")] = 2.25
                it[byteArrayPreferencesKey("bytes")] = byteArrayOf(0x0A, 0x1B)
            }
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `decodes every value type written by real DataStore`() = runTest {
        writeWithDataStore("settings.preferences_pb")

        dataSource.getPreferenceItems("settings.preferences_pb") shouldContainExactly listOf(
            PreferenceItem("bool", PreferenceValue.BooleanValue(true)),
            PreferenceItem("bytes", PreferenceValue.BytesValue(byteArrayOf(0x0A, 0x1B))),
            PreferenceItem("double", PreferenceValue.DoubleValue(2.25)),
            PreferenceItem("float", PreferenceValue.FloatValue(1.5f)),
            PreferenceItem("int", PreferenceValue.IntValue(-42)),
            PreferenceItem("long", PreferenceValue.LongValue(Long.MAX_VALUE)),
            PreferenceItem("set", PreferenceValue.StringSetValue(setOf("a", "b"))),
            PreferenceItem("string", PreferenceValue.StringValue("héllo")),
        )
    }

    @Test
    fun `lists only preferences_pb files with item counts`() = runTest {
        writeWithDataStore("settings.preferences_pb")
        File(dir, "proto.pb").writeBytes(byteArrayOf(1))

        val files = dataSource.getPreferenceFiles()

        files.map { Triple(it.name, it.itemCount, it.source) } shouldContainExactly listOf(
            Triple("settings.preferences_pb", 8, PreferenceSource.DATASTORE),
        )
    }

    @Test
    fun `returns no items for a corrupt file`() {
        mockkStatic(Log::class)
        try {
            every { Log.w(any<String>(), any<String>(), any()) } returns 0
            File(dir, "broken.preferences_pb").writeBytes(byteArrayOf(0x0A, 0x7F))

            dataSource.getPreferenceItems("broken.preferences_pb").shouldBeEmpty()
        } finally {
            unmockkStatic(Log::class)
        }
    }

    @Test
    fun `returns nothing when the datastore directory is missing`() {
        DataStoreDataSource { File(dir, "missing") }.getPreferenceFiles().shouldBeEmpty()
    }
}
