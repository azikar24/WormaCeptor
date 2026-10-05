package com.azikar24.wormaceptor.core.ui.navigation

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ServiceLoader

/**
 * Discovers feature navigation contributors via [ServiceLoader] and builds the NavHost.
 *
 * The classpath scan runs on [Dispatchers.IO] (StrictMode disk read) and starts on first access
 * to this object. UI must wait for [contributors] to be non-null before calling [contributeAll].
 */
object FeatureRegistry {

    private val _contributors = MutableStateFlow<List<FeatureNavigationContributor>?>(null)

    /** Discovered contributors, or null while the scan is still running. */
    val contributors: StateFlow<List<FeatureNavigationContributor>?> = _contributors.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _contributors.value = ServiceLoader.load(FeatureNavigationContributor::class.java).toList()
        }
    }

    /** Starts the scan early. The scan itself is triggered by object initialisation. */
    fun preload() {
        // Touching the object runs its initializer, which starts the scan.
    }

    /** Invokes all discovered contributors to build navigation destinations. */
    fun contributeAll(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        val loaded = checkNotNull(_contributors.value) { "FeatureRegistry not loaded yet; wait for contributors" }
        loaded.forEach { it.contribute(builder, navController, context, onBack) }
    }
}
