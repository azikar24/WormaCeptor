/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import com.azikar24.wormaceptor.domain.entities.ViewBounds
import com.azikar24.wormaceptor.domain.entities.ViewNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Engine that captures and analyzes view hierarchies.
 *
 * Recursively traverses the view tree starting from a root view,
 * capturing properties of each view including bounds, visibility,
 * alpha, elevation, and resource IDs.
 *
 * Features:
 * - Recursive hierarchy capture
 * - Property extraction (class name, bounds, visibility, alpha, elevation)
 * - Resource ID resolution
 * - Content description capture
 * - Depth tracking for 3D visualization
 * - StateFlow exposure for reactive UI updates
 */
class ViewHierarchyEngine {

    // Current captured hierarchy root
    private val _rootNode = MutableStateFlow<ViewNode?>(null)
    val rootNode: StateFlow<ViewNode?> = _rootNode.asStateFlow()

    // Capture timestamp
    private val _captureTimestamp = MutableStateFlow(0L)
    val captureTimestamp: StateFlow<Long> = _captureTimestamp.asStateFlow()

    // Total view count in current hierarchy
    private val _viewCount = MutableStateFlow(0)
    val viewCount: StateFlow<Int> = _viewCount.asStateFlow()

    // Maximum depth in current hierarchy
    private val _maxDepth = MutableStateFlow(0)
    val maxDepth: StateFlow<Int> = _maxDepth.asStateFlow()

    /**
     * Captures the entire view hierarchy starting from the given root view.
     *
     * This method must be called on the main/UI thread since it accesses
     * View properties.
     *
     * @param rootView The root view to start capturing from
     */
    fun captureHierarchy(rootView: View) {
        val startTime = System.currentTimeMillis()
        var totalCount = 0
        var maxDepthFound = 0

        fun captureNode(view: View, depth: Int): ViewNode {
            totalCount++
            if (depth > maxDepthFound) {
                maxDepthFound = depth
            }

            // Capture bounds
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            val bounds = ViewBounds(rect.left, rect.top, rect.right, rect.bottom)

            // Capture resource ID
            val resourceId = try {
                if (view.id != View.NO_ID) {
                    view.resources.getResourceEntryName(view.id)?.let { "@id/$it" }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

            // Capture elevation
            val elevation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.elevation
            } else {
                0f
            }

            // Build properties map
            val properties = buildPropertiesMap(view)

            // Capture children
            val children = if (view is ViewGroup) {
                (0 until view.childCount).map { index ->
                    captureNode(view.getChildAt(index), depth + 1)
                }
            } else {
                emptyList()
            }

            return ViewNode(
                id = view.hashCode(),
                className = view.javaClass.name,
                simpleClassName = view.javaClass.simpleName,
                resourceId = resourceId,
                contentDescription = view.contentDescription?.toString(),
                bounds = bounds,
                visibility = view.visibility,
                alpha = view.alpha,
                elevation = elevation,
                depth = depth,
                children = children,
                properties = properties,
            )
        }

        val rootNode = captureNode(rootView, 0)

        _rootNode.value = rootNode
        _captureTimestamp.value = startTime
        _viewCount.value = totalCount
        _maxDepth.value = maxDepthFound
    }

    /**
     * Clears the current hierarchy capture.
     */
    fun clear() {
        _rootNode.value = null
        _captureTimestamp.value = 0L
        _viewCount.value = 0
        _maxDepth.value = 0
    }

    /**
     * Finds a ViewNode by its ID in the current hierarchy.
     *
     * @param nodeId The node ID to search for
     * @return The matching ViewNode or null if not found
     */
    fun findNodeById(nodeId: Int): ViewNode? {
        val root = _rootNode.value ?: return null
        return findNodeInTree(root, nodeId)
    }

    private fun findNodeInTree(node: ViewNode, targetId: Int): ViewNode? {
        if (node.id == targetId) return node
        for (child in node.children) {
            val found = findNodeInTree(child, targetId)
            if (found != null) return found
        }
        return null
    }

    /**
     * Returns a flattened list of all nodes in the hierarchy.
     */
    fun getAllNodes(): List<ViewNode> {
        val root = _rootNode.value ?: return emptyList()
        val result = mutableListOf<ViewNode>()
        collectNodes(root, result)
        return result
    }

    private fun collectNodes(node: ViewNode, result: MutableList<ViewNode>) {
        result.add(node)
        for (child in node.children) {
            collectNodes(child, result)
        }
    }

    private fun buildPropertiesMap(view: View): Map<String, String> {
        val properties = mutableMapOf<String, String>()

        // Basic properties
        properties["width"] = "${view.width}px"
        properties["height"] = "${view.height}px"
        properties["x"] = "${view.x}"
        properties["y"] = "${view.y}"
        properties["translationX"] = "${view.translationX}"
        properties["translationY"] = "${view.translationY}"
        properties["scaleX"] = "${view.scaleX}"
        properties["scaleY"] = "${view.scaleY}"
        properties["rotation"] = "${view.rotation}"
        properties["rotationX"] = "${view.rotationX}"
        properties["rotationY"] = "${view.rotationY}"
        properties["pivotX"] = "${view.pivotX}"
        properties["pivotY"] = "${view.pivotY}"

        // Padding
        properties["paddingLeft"] = "${view.paddingLeft}px"
        properties["paddingTop"] = "${view.paddingTop}px"
        properties["paddingRight"] = "${view.paddingRight}px"
        properties["paddingBottom"] = "${view.paddingBottom}px"

        // Visibility
        properties["visibility"] = when (view.visibility) {
            View.VISIBLE -> "VISIBLE"
            View.INVISIBLE -> "INVISIBLE"
            View.GONE -> "GONE"
            else -> "UNKNOWN"
        }

        // State
        properties["enabled"] = "${view.isEnabled}"
        properties["clickable"] = "${view.isClickable}"
        properties["focusable"] = "${view.isFocusable}"
        properties["selected"] = "${view.isSelected}"
        properties["activated"] = "${view.isActivated}"

        // Background
        if (view.background != null) {
            properties["hasBackground"] = "true"
            properties["backgroundClass"] = view.background.javaClass.simpleName
        } else {
            properties["hasBackground"] = "false"
        }

        // Elevation and Z (API 21+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            properties["elevation"] = "${view.elevation}px"
            properties["translationZ"] = "${view.translationZ}px"
            properties["z"] = "${view.z}px"
        }

        // Layout params
        view.layoutParams?.let { params ->
            properties["layoutWidth"] = when (params.width) {
                ViewGroup.LayoutParams.MATCH_PARENT -> "MATCH_PARENT"
                ViewGroup.LayoutParams.WRAP_CONTENT -> "WRAP_CONTENT"
                else -> "${params.width}px"
            }
            properties["layoutHeight"] = when (params.height) {
                ViewGroup.LayoutParams.MATCH_PARENT -> "MATCH_PARENT"
                ViewGroup.LayoutParams.WRAP_CONTENT -> "WRAP_CONTENT"
                else -> "${params.height}px"
            }
        }

        // Tag
        view.tag?.let { tag ->
            properties["tag"] = tag.toString()
        }

        return properties
    }

    companion object {
        /** Default maximum depth to traverse (prevent infinite loops in edge cases) */
        const val MAX_DEPTH = 100
    }
}
