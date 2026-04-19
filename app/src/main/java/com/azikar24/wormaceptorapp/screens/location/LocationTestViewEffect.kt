package com.azikar24.wormaceptorapp.screens.location

sealed class LocationTestViewEffect {
    data object RequestLocationPermission : LocationTestViewEffect()
    data object OpenMockLocationTool : LocationTestViewEffect()
}
