/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing and modifying SharedPreferences files.
 */
interface PreferencesRepository {

    /**
     * Observes all SharedPreferences files in the app's data directory.
     * Emits updates when files are added, removed, or modified.
     */
    fun observePreferenceFiles(): Flow<List<PreferenceFile>>

    /**
     * Observes all key-value pairs in a specific SharedPreferences file.
     * Emits updates when the file content changes.
     *
     * @param fileName The name of the SharedPreferences file (without .xml extension)
     */
    fun observePreferenceItems(fileName: String): Flow<List<PreferenceItem>>

    /**
     * Gets a specific preference value by key from a file.
     *
     * @param fileName The name of the SharedPreferences file
     * @param key The preference key
     * @return The preference value, or null if not found
     */
    suspend fun getPreference(fileName: String, key: String): PreferenceValue?

    /**
     * Sets a preference value. Creates or updates the key.
     *
     * @param fileName The name of the SharedPreferences file
     * @param key The preference key
     * @param value The new value to set
     */
    suspend fun setPreference(fileName: String, key: String, value: PreferenceValue)

    /**
     * Deletes a specific preference key from a file.
     *
     * @param fileName The name of the SharedPreferences file
     * @param key The preference key to delete
     */
    suspend fun deletePreference(fileName: String, key: String)

    /**
     * Clears all preferences from a file.
     *
     * @param fileName The name of the SharedPreferences file
     */
    suspend fun clearFile(fileName: String)
}
