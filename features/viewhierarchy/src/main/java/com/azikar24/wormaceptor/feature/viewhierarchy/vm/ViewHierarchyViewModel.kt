/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewhierarchy.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.ViewHierarchyEngine
import com.azikar24.wormaceptor.domain.entities.ViewNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the View Hierarchy Inspector screen.
 *
 * Provides access to captured view hierarchy data with search
 * and filtering capabilities.
 */
class ViewHierarchyViewModel(
    private val engine: ViewHierarchyEngine,
) : ViewModel() {

    // Root node of the captured hierarchy
    val rootNode: StateFlow<ViewNode?> = engine.rootNode
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null,
        )

    // Total view count
    val viewCount: StateFlow<Int> = engine.viewCount
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0,
        )

    // Maximum depth
    val maxDepth: StateFlow<Int> = engine.maxDepth
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0,
        )

    // Capture timestamp
    val captureTimestamp: StateFlow<Long> = engine.captureTimestamp
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0L,
        )

    // Currently selected node for detail view
    private val _selectedNode = MutableStateFlow<ViewNode?>(null)
    val selectedNode: StateFlow<ViewNode?> = _selectedNode.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Expanded nodes in tree view
    private val _expandedNodeIds = MutableStateFlow<Set<Int>>(emptySet())
    val expandedNodeIds: StateFlow<Set<Int>> = _expandedNodeIds.asStateFlow()

    // 3D view mode
    private val _is3DMode = MutableStateFlow(false)
    val is3DMode: StateFlow<Boolean> = _is3DMode.asStateFlow()

    // Flattened list of all nodes for search
    val allNodes: StateFlow<List<ViewNode>> = engine.rootNode
        .map { root -> if (root != null) flattenHierarchy(root) else emptyList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    /**
     * Sets the search query for filtering nodes.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Selects a node to view its details.
     */
    fun selectNode(node: ViewNode) {
        _selectedNode.value = node
    }

    /**
     * Dismisses the detail view.
     */
    fun dismissDetail() {
        _selectedNode.value = null
    }

    /**
     * Toggles the expanded state of a node.
     */
    fun toggleNodeExpanded(nodeId: Int) {
        val current = _expandedNodeIds.value
        _expandedNodeIds.value = if (nodeId in current) {
            current - nodeId
        } else {
            current + nodeId
        }
    }

    /**
     * Expands all nodes in the tree.
     */
    fun expandAll() {
        val allIds = allNodes.value.map { it.id }.toSet()
        _expandedNodeIds.value = allIds
    }

    /**
     * Collapses all nodes in the tree.
     */
    fun collapseAll() {
        _expandedNodeIds.value = emptySet()
    }

    /**
     * Toggles 3D view mode.
     */
    fun toggle3DMode() {
        _is3DMode.value = !_is3DMode.value
    }

    /**
     * Clears the current hierarchy capture.
     */
    fun clear() {
        engine.clear()
        _selectedNode.value = null
        _expandedNodeIds.value = emptySet()
    }

    /**
     * Finds a node by its ID.
     */
    fun findNodeById(nodeId: Int): ViewNode? {
        return engine.findNodeById(nodeId)
    }

    /**
     * Flattens the hierarchy into a list for searching.
     */
    private fun flattenHierarchy(node: ViewNode): List<ViewNode> {
        val result = mutableListOf<ViewNode>()
        collectNodes(node, result)
        return result
    }

    private fun collectNodes(node: ViewNode, result: MutableList<ViewNode>) {
        result.add(node)
        for (child in node.children) {
            collectNodes(child, result)
        }
    }
}
