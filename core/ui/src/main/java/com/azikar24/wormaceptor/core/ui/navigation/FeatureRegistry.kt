package com.azikar24.wormaceptor.core.ui.navigation

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.ServiceLoader

/**
 * Discovers feature navigation contributors via [ServiceLoader] and builds the NavHost.
 *
 * The classpath scan runs on [Dispatchers.IO] to avoid a StrictMode disk-read
 * violation on the main thread. The background coroutine starts during object
 * initialisation, so by the time [contributeAll] is called during Compose
 * composition the result is typically already cached.
 */
object FeatureRegistry {

    private val contributors: Deferred<List<FeatureNavigationContributor>> =
        CoroutineScope(Dispatchers.IO).async {
            ServiceLoader.load(FeatureNavigationContributor::class.java).toList()
        }

    /** Invokes all discovered contributors to build navigation destinations. */
    fun contributeAll(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        val loaded = runBlocking { contributors.await() }
        loaded.forEach { it.contribute(builder, navController, context, onBack) }
    }
}
