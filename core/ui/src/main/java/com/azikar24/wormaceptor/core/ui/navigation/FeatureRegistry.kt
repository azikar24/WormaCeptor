package com.azikar24.wormaceptor.core.ui.navigation

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import java.util.concurrent.CopyOnWriteArrayList

/** Thread-safe registry of feature navigation contributors for dynamic NavHost construction. */
object FeatureRegistry {

    private val contributors = CopyOnWriteArrayList<FeatureNavigationContributor>()

    /** Adds a navigation contributor to the registry. */
    fun register(contributor: FeatureNavigationContributor) {
        contributors.add(contributor)
    }

    /** Invokes all registered contributors to build navigation destinations. */
    fun contributeAll(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        contributors.forEach { it.contribute(builder, navController, context, onBack) }
    }

    /** Removes all registered contributors. */
    fun clear() {
        contributors.clear()
    }
}
