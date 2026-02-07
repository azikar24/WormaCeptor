package com.azikar24.wormaceptor.feature.location.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private const val TAG = "LocationDataSource"

private val Context.locationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wormaceptor_location_presets",
)

/**
 * Data source for persisting location presets and mock location state.
 * Uses DataStore for persistence.
 */
class LocationDataSource(private val context: Context) {

    private val userPresetsKey = stringPreferencesKey("user_presets")
    private val currentMockLocationKey = stringPreferencesKey("current_mock_location")

    /**
     * Built-in location presets that cannot be deleted.
     */
    val builtInPresets: List<LocationPreset> = listOf(
        LocationPreset(
            id = "builtin_new_york",
            name = "New York",
            location = MockLocation.from(40.7128, -74.0060, "New York"),
            isBuiltIn = true,
        ),
        LocationPreset(
            id = "builtin_london",
            name = "London",
            location = MockLocation.from(51.5074, -0.1278, "London"),
            isBuiltIn = true,
        ),
        LocationPreset(
            id = "builtin_tokyo",
            name = "Tokyo",
            location = MockLocation.from(35.6762, 139.6503, "Tokyo"),
            isBuiltIn = true,
        ),
        LocationPreset(
            id = "builtin_sydney",
            name = "Sydney",
            location = MockLocation.from(-33.8688, 151.2093, "Sydney"),
            isBuiltIn = true,
        ),
        LocationPreset(
            id = "builtin_paris",
            name = "Paris",
            location = MockLocation.from(48.8566, 2.3522, "Paris"),
            isBuiltIn = true,
        ),
    )

    /**
     * Observes all presets (built-in + user-created).
     */
    fun observePresets(): Flow<List<LocationPreset>> {
        return context.locationDataStore.data.map { preferences ->
            val userPresets = preferences[userPresetsKey]?.let { parseUserPresets(it) } ?: emptyList()
            builtInPresets + userPresets
        }
    }

    /**
     * Saves a user-created preset.
     */
    suspend fun savePreset(preset: LocationPreset) {
        context.locationDataStore.edit { preferences ->
            val existingJson = preferences[userPresetsKey]
            val existingPresets = existingJson?.let { parseUserPresets(it) }?.toMutableList() ?: mutableListOf()

            // Remove existing preset with same ID if exists
            existingPresets.removeAll { it.id == preset.id }

            // Add the new preset
            existingPresets.add(preset.copy(isBuiltIn = false))

            preferences[userPresetsKey] = serializeUserPresets(existingPresets)
        }
    }

    /**
     * Deletes a user-created preset by ID.
     */
    suspend fun deletePreset(id: String) {
        // Cannot delete built-in presets
        if (builtInPresets.any { it.id == id }) return

        context.locationDataStore.edit { preferences ->
            val existingJson = preferences[userPresetsKey]
            val existingPresets = existingJson?.let { parseUserPresets(it) }?.toMutableList() ?: mutableListOf()

            existingPresets.removeAll { it.id == id }

            preferences[userPresetsKey] = serializeUserPresets(existingPresets)
        }
    }

    /**
     * Observes the current mock location state.
     */
    fun observeCurrentMockLocation(): Flow<MockLocation?> {
        return context.locationDataStore.data.map { preferences ->
            preferences[currentMockLocationKey]?.let { parseMockLocation(it) }
        }
    }

    /**
     * Sets or clears the current mock location.
     */
    suspend fun setCurrentMockLocation(location: MockLocation?) {
        context.locationDataStore.edit { preferences ->
            if (location == null) {
                preferences.remove(currentMockLocationKey)
            } else {
                preferences[currentMockLocationKey] = serializeMockLocation(location)
            }
        }
    }

    // JSON serialization helpers

    private fun serializeUserPresets(presets: List<LocationPreset>): String {
        val jsonArray = JSONArray()
        presets.forEach { preset ->
            val jsonObject = JSONObject().apply {
                put("id", preset.id)
                put("name", preset.name)
                put("latitude", preset.location.latitude)
                put("longitude", preset.location.longitude)
                put("altitude", preset.location.altitude)
                put("accuracy", preset.location.accuracy.toDouble())
                put("speed", preset.location.speed.toDouble())
                put("bearing", preset.location.bearing.toDouble())
                preset.location.name?.let { put("locationName", it) }
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun parseUserPresets(json: String): List<LocationPreset> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                LocationPreset(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    location = MockLocation(
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        altitude = obj.optDouble("altitude", 0.0),
                        accuracy = obj.optDouble("accuracy", 1.0).toFloat(),
                        speed = obj.optDouble("speed", 0.0).toFloat(),
                        bearing = obj.optDouble("bearing", 0.0).toFloat(),
                        name = obj.optString("locationName", null),
                    ),
                    isBuiltIn = false,
                )
            }
        } catch (e: JSONException) {
            Log.d(TAG, "Failed to parse user presets JSON", e)
            emptyList()
        }
    }

    private fun serializeMockLocation(location: MockLocation): String {
        return JSONObject().apply {
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("altitude", location.altitude)
            put("accuracy", location.accuracy.toDouble())
            put("speed", location.speed.toDouble())
            put("bearing", location.bearing.toDouble())
            put("timestamp", location.timestamp)
            location.name?.let { put("name", it) }
        }.toString()
    }

    private fun parseMockLocation(json: String): MockLocation? {
        return try {
            val obj = JSONObject(json)
            MockLocation(
                latitude = obj.getDouble("latitude"),
                longitude = obj.getDouble("longitude"),
                altitude = obj.optDouble("altitude", 0.0),
                accuracy = obj.optDouble("accuracy", 1.0).toFloat(),
                speed = obj.optDouble("speed", 0.0).toFloat(),
                bearing = obj.optDouble("bearing", 0.0).toFloat(),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                name = obj.optString("name", null),
            )
        } catch (e: JSONException) {
            Log.d(TAG, "Failed to parse mock location JSON", e)
            null
        }
    }
}
