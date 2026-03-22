package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [LocationSimulatorRepository] that persists location presets and mock state to SQLite. */
class RoomLocationSimulatorRepository(
    private val presetDao: LocationPresetDao,
    private val mockLocationDao: MockLocationDao,
) : LocationSimulatorRepository {

    override fun getPresets(): Flow<List<LocationPreset>> {
        return presetDao.observeAll().map { entities ->
            val userPresets = entities.map { it.toDomain() }
            builtInPresets + userPresets.filter { preset -> !preset.isBuiltIn }
        }
    }

    override suspend fun savePreset(preset: LocationPreset) {
        presetDao.insert(LocationPresetEntity.fromDomain(preset.copy(isBuiltIn = false)))
    }

    override suspend fun deletePreset(id: String) {
        if (builtInPresets.any { it.id == id }) return
        presetDao.deleteById(id)
    }

    override fun getCurrentMockLocation(): Flow<MockLocation?> {
        return mockLocationDao.observe().map { it?.toDomain() }
    }

    override suspend fun setMockLocation(location: MockLocation?) {
        if (location == null) {
            mockLocationDao.clear()
        } else {
            mockLocationDao.upsert(MockLocationEntity.fromDomain(location))
        }
    }

    /** Built-in location presets shipped with the library. */
    companion object {
        /** Default location presets shipped with the app. */
        val builtInPresets: List<LocationPreset> = listOf(
            LocationPreset(
                id = "builtin_kuwait",
                name = "Kuwait City",
                location = MockLocation.from(29.3759, 47.9774, "Kuwait City"),
                isBuiltIn = true,
            ),
            LocationPreset(
                id = "builtin_cairo",
                name = "Cairo",
                location = MockLocation.from(30.0444, 31.2357, "Cairo"),
                isBuiltIn = true,
            ),
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
    }
}
