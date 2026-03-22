package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [LocationSimulatorRepository] implementation with built-in city presets. */
class InMemoryLocationSimulatorRepository : LocationSimulatorRepository {

    private val _presets = MutableStateFlow(builtInPresets)
    private val _mockLocation = MutableStateFlow<MockLocation?>(null)

    override fun getPresets(): Flow<List<LocationPreset>> {
        return _presets.map { it.toList() }
    }

    override suspend fun savePreset(preset: LocationPreset) {
        _presets.update { current ->
            current.filter { it.id != preset.id } + preset.copy(isBuiltIn = false)
        }
    }

    override suspend fun deletePreset(id: String) {
        if (builtInPresets.any { it.id == id }) return
        _presets.update { current -> current.filter { it.id != id } }
    }

    override fun getCurrentMockLocation(): Flow<MockLocation?> {
        return _mockLocation
    }

    override suspend fun setMockLocation(location: MockLocation?) {
        _mockLocation.value = location
    }

    /** Built-in location presets shipped with the library. */
    companion object {
        private val builtInPresets: List<LocationPreset> = listOf(
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
