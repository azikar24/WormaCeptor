package com.azikar24.wormaceptor.feature.mockrules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.feature.mockrules.ui.MockRuleEditorScreen
import com.azikar24.wormaceptor.feature.mockrules.ui.MockRulesScreen
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRuleEditorViewModel
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesEvent
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewModel
import org.koin.compose.koinInject

/**
 * Entry composable for the Mock Rules list screen.
 * Handles Koin injection and ViewModel creation.
 */
@Composable
fun MockRulesList(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToEditor: (String?) -> Unit = {},
) {
    val repository: MockRuleRepository = koinInject()
    val engine: MockEngine = koinInject()
    val factory = remember(repository, engine) { MockRulesViewModelFactory(repository, engine) }
    val viewModel: MockRulesViewModel = viewModel(factory = factory)

    val state by viewModel.uiState.collectAsState()

    MockRulesScreen(
        rules = state.rules,
        mockingEnabled = state.mockingEnabled,
        onToggleMocking = { viewModel.sendEvent(MockRulesEvent.ToggleMocking) },
        onToggleRule = { ruleId -> viewModel.sendEvent(MockRulesEvent.ToggleRule(ruleId)) },
        onDeleteRule = { ruleId -> viewModel.sendEvent(MockRulesEvent.DeleteRule(ruleId)) },
        onDeleteAll = { viewModel.sendEvent(MockRulesEvent.DeleteAllRules) },
        onAddRule = { onNavigateToEditor(null) },
        onEditRule = { ruleId -> onNavigateToEditor(ruleId) },
        onBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Entry composable for the Mock Rule editor screen.
 * Handles Koin injection and loading existing rule data.
 */
@Composable
fun MockRuleEditor(
    ruleId: String?,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
) {
    val repository: MockRuleRepository = koinInject()
    val engine: MockEngine = koinInject()
    val rulesFactory = remember(repository, engine) { MockRulesViewModelFactory(repository, engine) }
    val rulesViewModel: MockRulesViewModel = viewModel(factory = rulesFactory)
    val editorViewModel: MockRuleEditorViewModel = viewModel()

    var isLoaded by remember { mutableStateOf(ruleId == null || ruleId == "new") }

    LaunchedEffect(ruleId) {
        if (ruleId != null && ruleId != "new") {
            val existingRule = rulesViewModel.getRuleById(ruleId)
            editorViewModel.initialize(existingRule)
            isLoaded = true
        } else {
            editorViewModel.initialize(null)
        }
    }

    if (isLoaded) {
        MockRuleEditorScreen(
            viewModel = editorViewModel,
            onSave = {
                rulesViewModel.sendEvent(MockRulesEvent.SaveRule(editorViewModel.buildRule()))
                onNavigateBack()
            },
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}

/**
 * Factory for creating [MockRulesViewModel] instances.
 */
class MockRulesViewModelFactory(
    private val repository: MockRuleRepository,
    private val engine: MockEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MockRulesViewModel::class.java)) {
            return MockRulesViewModel(repository, engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
