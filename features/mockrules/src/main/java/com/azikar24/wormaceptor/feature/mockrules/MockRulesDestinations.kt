package com.azikar24.wormaceptor.feature.mockrules

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.feature.mockrules.ui.MockRuleEditorContent
import com.azikar24.wormaceptor.feature.mockrules.ui.MockRulesScreen
import com.azikar24.wormaceptor.feature.mockrules.vm.EditorNotice
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesEffect
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewEvent
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewModel
import kotlinx.coroutines.launch
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
    val factory = remember(repository, engine) {
        MockRulesViewModelFactory(repository, engine, CoreHolder.queryEngine)
    }
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
    transactionId: String? = null,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveFailedMessage = stringResource(R.string.mock_editor_save_failed)
    val transactionNotFoundMessage = stringResource(R.string.mock_editor_transaction_not_found)
    val bodyOmittedMessage = stringResource(R.string.mock_editor_body_omitted)

    // The back stack entry id is stable across Activity recreation and unique per visit, so the VM
    // reloads when the editor is reopened but keeps edits on rotation.
    LaunchedEffect(ruleId, transactionId) {
        val event = if (transactionId != null) {
            MockRulesViewEvent.Editor.LoadFromTransaction(transactionId, backStackEntry.id)
        } else {
            MockRulesViewEvent.Editor.LoadRule(ruleId, backStackEntry.id)
        }
        viewModel.sendEvent(event)
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is MockRulesEffect.NavigateBack -> navController.popBackStack()
                is MockRulesEffect.SaveFailed -> scope.launch { snackbarHostState.showSnackbar(saveFailedMessage) }
            }
        },
    ) { state, onEvent ->
        val notice = state.editor.notice
        LaunchedEffect(notice) {
            val message = when (notice) {
                EditorNotice.TransactionNotFound -> transactionNotFoundMessage
                EditorNotice.ResponseBodyOmitted -> bodyOmittedMessage
                null -> return@LaunchedEffect
            }
            snackbarHostState.showSnackbar(message)
            onEvent(MockRulesViewEvent.Editor.NoticeShown)
        }
        if (state.editor.isLoaded) {
            MockRuleEditorContent(
                state = state.editor,
                onEvent = onEvent,
                onBack = { navController.popBackStack() },
                snackbarHostState = snackbarHostState,
                modifier = modifier,
            )
        }
    }
}
