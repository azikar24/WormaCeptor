package com.azikar24.wormaceptor.feature.location

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.feature.location.ui.LocationScreen
import com.azikar24.wormaceptor.feature.location.vm.LocationViewEffect
import com.azikar24.wormaceptor.feature.location.vm.LocationViewEvent
import com.azikar24.wormaceptor.feature.location.vm.LocationViewModel
import com.google.auto.service.AutoService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.osmdroid.util.GeoPoint

@AutoService(FeatureNavigationContributor::class)
class LocationNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Location.route) {
            LocationDestination(onBack = onBack)
        }
    }
}

@Suppress("ViewModelForwarding")
@Composable
private fun LocationDestination(onBack: (() -> Unit)? = null) {
    val engine: LocationSimulatorEngine = koinInject()
    val repository: LocationSimulatorRepository = koinInject()
    val context = androidx.compose.ui.platform.LocalContext.current
    val factory = remember { LocationViewModelFactory(repository, engine, context) }
    val viewModel: LocationViewModel = viewModel(factory = factory)

    val snackBarState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.sendEvent(LocationViewEvent.RefreshMockLocationAvailability)
                    viewModel.sendEvent(LocationViewEvent.StartRealLocationUpdates)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.sendEvent(LocationViewEvent.StopRealLocationUpdates)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.sendEvent(LocationViewEvent.StopRealLocationUpdates)
        }
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is LocationViewEffect.ShowError -> scope.launch { snackBarState.showSnackbar(effect.message) }
                is LocationViewEffect.ShowSuccess -> scope.launch { snackBarState.showSnackbar(effect.message) }
            }
        },
    ) { state, onEvent ->
        val presets by viewModel.presets.collectAsState()
        val currentMockLocation by viewModel.currentMockLocation.collectAsState()
        val isMockEnabled by viewModel.isMockEnabled.collectAsState()
        val isInputValid by viewModel.isInputValid.collectAsState()
        val realDeviceLocation by viewModel.realDeviceLocation.collectAsState()

        val realGeoPoint = realDeviceLocation?.let {
            GeoPoint(it.latitude, it.longitude)
        }

        val mergedState = state.copy(
            presets = presets,
            currentMockLocation = currentMockLocation,
            isMockEnabled = isMockEnabled,
            isInputValid = isInputValid,
            realDeviceLocation = realGeoPoint,
        )

        LocationScreen(
            state = mergedState,
            onEvent = onEvent,
            onBack = onBack,
            snackBarHostState = snackBarState,
        )
    }
}
