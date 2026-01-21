/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewhierarchy

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.ViewHierarchyEngine
import com.azikar24.wormaceptor.feature.viewhierarchy.ui.ViewHierarchyScreen
import com.azikar24.wormaceptor.feature.viewhierarchy.vm.ViewHierarchyViewModel
import kotlinx.collections.immutable.toImmutableSet

/**
 * Entry point for the View Hierarchy Inspector feature.
 * Provides factory methods and composable entry point.
 */
object ViewHierarchyFeature {

    /**
     * Creates a ViewHierarchyEngine instance.
     * Use this in your dependency injection setup or as a singleton.
     *
     * @return A new ViewHierarchyEngine instance
     */
    fun createEngine(): ViewHierarchyEngine {
        return ViewHierarchyEngine()
    }

    /**
     * Creates a ViewHierarchyViewModel factory for use with viewModel().
     *
     * @param engine The ViewHierarchyEngine instance to use
     * @return A ViewModelProvider.Factory for creating ViewHierarchyViewModel
     */
    fun createViewModelFactory(engine: ViewHierarchyEngine): ViewHierarchyViewModelFactory {
        return ViewHierarchyViewModelFactory(engine)
    }

    /**
     * Captures the view hierarchy starting from the given root view.
     * Call this from the UI thread before displaying the inspector.
     *
     * @param engine The ViewHierarchyEngine instance
     * @param rootView The root view to capture from
     */
    fun captureHierarchy(engine: ViewHierarchyEngine, rootView: View) {
        engine.captureHierarchy(rootView)
    }
}

/**
 * Factory for creating ViewHierarchyViewModel instances.
 */
class ViewHierarchyViewModelFactory(
    private val engine: ViewHierarchyEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewHierarchyViewModel::class.java)) {
            return ViewHierarchyViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the View Hierarchy Inspector feature.
 * Displays the captured view hierarchy with search and detail capabilities.
 *
 * @param modifier Modifier for the root layout
 * @param onNavigateBack Optional callback for back navigation
 */
@Composable
fun ViewHierarchyInspector(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine = remember { ViewHierarchyFeature.createEngine() }
    val factory = remember { ViewHierarchyFeature.createViewModelFactory(engine) }
    val viewModel: ViewHierarchyViewModel = viewModel(factory = factory)

    // Collect state
    val rootNode by viewModel.rootNode.collectAsState()
    val viewCount by viewModel.viewCount.collectAsState()
    val maxDepth by viewModel.maxDepth.collectAsState()
    val captureTimestamp by viewModel.captureTimestamp.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val expandedNodeIds by viewModel.expandedNodeIds.collectAsState()
    val is3DMode by viewModel.is3DMode.collectAsState()

    ViewHierarchyScreen(
        rootNode = rootNode,
        viewCount = viewCount,
        maxDepth = maxDepth,
        captureTimestamp = captureTimestamp,
        selectedNode = selectedNode,
        searchQuery = searchQuery,
        expandedNodeIds = expandedNodeIds,
        is3DMode = is3DMode,
        onSearchQueryChanged = viewModel::setSearchQuery,
        onNodeSelected = viewModel::selectNode,
        onDismissDetail = viewModel::dismissDetail,
        onToggleNodeExpanded = viewModel::toggleNodeExpanded,
        onExpandAll = viewModel::expandAll,
        onCollapseAll = viewModel::collapseAll,
        onToggle3DMode = viewModel::toggle3DMode,
        onClear = viewModel::clear,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Main composable with an externally provided engine.
 * Use this when you need to share the engine for capturing hierarchies.
 *
 * @param engine The shared ViewHierarchyEngine instance
 * @param modifier Modifier for the root layout
 * @param onNavigateBack Optional callback for back navigation
 */
@Composable
fun ViewHierarchyInspector(
    engine: ViewHierarchyEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val factory = remember(engine) { ViewHierarchyFeature.createViewModelFactory(engine) }
    val viewModel: ViewHierarchyViewModel = viewModel(factory = factory)

    // Collect state
    val rootNode by viewModel.rootNode.collectAsState()
    val viewCount by viewModel.viewCount.collectAsState()
    val maxDepth by viewModel.maxDepth.collectAsState()
    val captureTimestamp by viewModel.captureTimestamp.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val expandedNodeIds by viewModel.expandedNodeIds.collectAsState()
    val is3DMode by viewModel.is3DMode.collectAsState()

    ViewHierarchyScreen(
        rootNode = rootNode,
        viewCount = viewCount,
        maxDepth = maxDepth,
        captureTimestamp = captureTimestamp,
        selectedNode = selectedNode,
        searchQuery = searchQuery,
        expandedNodeIds = expandedNodeIds,
        is3DMode = is3DMode,
        onSearchQueryChanged = viewModel::setSearchQuery,
        onNodeSelected = viewModel::selectNode,
        onDismissDetail = viewModel::dismissDetail,
        onToggleNodeExpanded = viewModel::toggleNodeExpanded,
        onExpandAll = viewModel::expandAll,
        onCollapseAll = viewModel::collapseAll,
        onToggle3DMode = viewModel::toggle3DMode,
        onClear = viewModel::clear,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
