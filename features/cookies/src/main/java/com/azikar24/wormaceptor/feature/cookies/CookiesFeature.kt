/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.domain.contracts.CookiesRepository
import com.azikar24.wormaceptor.feature.cookies.data.CookiesDataSource
import com.azikar24.wormaceptor.feature.cookies.data.CookiesRepositoryImpl
import com.azikar24.wormaceptor.feature.cookies.ui.CookieDetailScreen
import com.azikar24.wormaceptor.feature.cookies.ui.CookiesListScreen
import com.azikar24.wormaceptor.feature.cookies.vm.CookiesViewModel

/**
 * Entry point for the HTTP Cookies Manager feature.
 * Provides a factory method and composable navigation host.
 */
object CookiesFeature {

    /**
     * Creates a CookiesRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): CookiesRepository {
        val dataSource = CookiesDataSource(context.applicationContext)
        return CookiesRepositoryImpl(dataSource)
    }

    /**
     * Creates a CookiesViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(repository: CookiesRepository): CookiesViewModelFactory {
        return CookiesViewModelFactory(repository)
    }
}

/**
 * Factory for creating CookiesViewModel instances.
 */
class CookiesViewModelFactory(
    private val repository: CookiesRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookiesViewModel::class.java)) {
            return CookiesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Cookies Manager feature.
 * Handles navigation between list and detail screens.
 */
@Composable
fun CookiesInspector(
    context: Context,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val repository = remember { CookiesFeature.createRepository(context) }
    val factory = remember { CookiesFeature.createViewModelFactory(repository) }
    val viewModel: CookiesViewModel = viewModel(factory = factory)

    val navController = rememberNavController()

    CookiesNavHost(
        navController = navController,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
private fun CookiesNavHost(
    navController: NavHostController,
    viewModel: CookiesViewModel,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val domains by viewModel.cookieDomains.collectAsState()
    val expandedDomains by viewModel.expandedDomains.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCookie by viewModel.selectedCookie.collectAsState()
    val totalCookieCount by viewModel.totalCookieCount.collectAsState()
    val totalDomainCount by viewModel.totalDomainCount.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "list",
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
    ) {
        composable("list") {
            CookiesListScreen(
                domains = domains,
                expandedDomains = expandedDomains,
                searchQuery = searchQuery,
                totalCookieCount = totalCookieCount,
                totalDomainCount = totalDomainCount,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onToggleDomain = viewModel::toggleDomainExpanded,
                onExpandAll = viewModel::expandAllDomains,
                onCollapseAll = viewModel::collapseAllDomains,
                onCookieClick = { cookie ->
                    viewModel.selectCookie(cookie)
                    navController.navigate("detail")
                },
                onDeleteDomain = viewModel::deleteAllCookiesForDomain,
                onClearAll = viewModel::clearAllCookies,
            )
        }

        composable("detail") {
            val cookie = selectedCookie ?: return@composable

            CookieDetailScreen(
                cookie = cookie,
                onBack = {
                    viewModel.clearCookieSelection()
                    navController.popBackStack()
                },
                onDelete = {
                    viewModel.deleteCookie(cookie)
                    navController.popBackStack()
                },
            )
        }
    }
}
