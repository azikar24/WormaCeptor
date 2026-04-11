package com.azikar24.wormaceptor.feature.mockrules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.feature.mockrules.ui.MockRuleEditorContent
import com.azikar24.wormaceptor.feature.mockrules.ui.MockRulesScreen
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesEffect
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewEvent
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewModel
import org.koin.compose.koinInject

@Composable
internal fun graphScopedViewModel(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
): MockRulesViewModel {
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry("mockrules_graph")
    }
    val repository: MockRuleRepository = koinInject()
    val engine: MockEngine = koinInject()
    val factory = remember(repository, engine) { MockRulesViewModelFactory(repository, engine) }
    return viewModel(viewModelStoreOwner = graphEntry, factory = factory)
}

@Composable
internal fun MockRulesListDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController)

    BaseScreen(viewModel) { state, onEvent ->
        MockRulesScreen(
            state = state,
            onEvent = onEvent,
            onNavigateToEditor = { ruleId ->
                if (ruleId != null) {
                    navController.navigate(WormaCeptorNavKeys.MockRuleEditor.createRoute(ruleId))
                } else {
                    navController.navigate(WormaCeptorNavKeys.MockRuleEditor.createNewRoute())
                }
            },
            onBack = onBack,
            modifier = modifier,
        )
    }
}

@Composable
internal fun MockRuleEditorDestination(
    ruleId: String?,
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController)

    LaunchedEffect(ruleId) {
        viewModel.sendEvent(MockRulesViewEvent.Editor.LoadRule(ruleId))
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is MockRulesEffect.NavigateBack -> navController.popBackStack()
            }
        },
    ) { state, onEvent ->
        if (state.editor.isLoaded) {
            MockRuleEditorContent(
                state = state.editor,
                onEvent = onEvent,
                onBack = { navController.popBackStack() },
                modifier = modifier,
            )
        }
    }
}
