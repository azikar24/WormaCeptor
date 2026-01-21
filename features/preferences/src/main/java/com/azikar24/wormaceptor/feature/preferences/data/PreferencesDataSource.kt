/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.preferences.data

import android.content.Context
import android.content.SharedPreferences
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import java.io.File

/**
 * Data source for reading and writing SharedPreferences files.
 * Provides low-level access to the app's SharedPreferences directory.
 */
class PreferencesDataSource(private val context: Context) {

    private val prefsDir: File
        get() = File(context.applicationInfo.dataDir, "shared_prefs")

    /**
     * Gets all SharedPreferences file names in the app's data directory.
     */
    fun getPreferenceFileNames(): List<String> {
        if (!prefsDir.exists() || !prefsDir.isDirectory) {
            return emptyList()
        }
        return prefsDir.listFiles()
            ?.filter { it.isFile && it.extension == "xml" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    /**
     * Gets all SharedPreferences files with their item counts.
     */
    fun getPreferenceFiles(): List<PreferenceFile> {
        return getPreferenceFileNames().map { fileName ->
            val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            PreferenceFile(
                name = fileName,
                itemCount = prefs.all.size,
            )
        }
    }

    /**
     * Gets all key-value pairs from a specific SharedPreferences file.
     */
    fun getPreferenceItems(fileName: String): List<PreferenceItem> {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return prefs.all.mapNotNull { (key, value) ->
            PreferenceValue.fromAny(value)?.let { prefValue ->
                PreferenceItem(key = key, value = prefValue)
            }
        }.sortedBy { it.key.lowercase() }
    }

    /**
     * Gets a specific preference value.
     */
    fun getPreference(fileName: String, key: String): PreferenceValue? {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val value = prefs.all[key]
        return PreferenceValue.fromAny(value)
    }

    /**
     * Sets a preference value.
     */
    fun setPreference(fileName: String, key: String, value: PreferenceValue) {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        prefs.edit().apply {
            when (value) {
                is PreferenceValue.StringValue -> putString(key, value.value)
                is PreferenceValue.IntValue -> putInt(key, value.value)
                is PreferenceValue.LongValue -> putLong(key, value.value)
                is PreferenceValue.FloatValue -> putFloat(key, value.value)
                is PreferenceValue.BooleanValue -> putBoolean(key, value.value)
                is PreferenceValue.StringSetValue -> putStringSet(key, value.value)
            }
        }.apply()
    }

    /**
     * Deletes a specific preference key.
     */
    fun deletePreference(fileName: String, key: String) {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        prefs.edit().remove(key).apply()
    }

    /**
     * Clears all preferences from a file.
     */
    fun clearFile(fileName: String) {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Registers a listener for changes to a specific SharedPreferences file.
     */
    fun registerChangeListener(
        fileName: String,
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ): SharedPreferences {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return prefs
    }

    /**
     * Unregisters a listener from a specific SharedPreferences file.
     */
    fun unregisterChangeListener(
        prefs: SharedPreferences,
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
