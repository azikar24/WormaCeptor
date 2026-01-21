/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents rectangular bounds in screen coordinates.
 * Pure Kotlin replacement for android.graphics.Rect.
 */
data class ViewBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top

    companion object {
        fun empty(): ViewBounds = ViewBounds(0, 0, 0, 0)
    }
}

/**
 * Represents a node in the view hierarchy tree.
 *
 * Used for 3D view hierarchy visualization and inspection.
 * Each node contains information about a View and its children.
 *
 * @property id Unique identifier for the view (View.hashCode())
 * @property className Full class name of the view (e.g., "android.widget.TextView")
 * @property simpleClassName Simple class name without package (e.g., "TextView")
 * @property resourceId Resource ID string if available (e.g., "@id/title")
 * @property contentDescription Accessibility content description if set
 * @property bounds View bounds in screen coordinates (as ViewBounds)
 * @property visibility View visibility state (View.VISIBLE, View.INVISIBLE, View.GONE)
 * @property alpha View alpha value (0.0 to 1.0)
 * @property elevation View elevation in pixels
 * @property depth Depth in the view hierarchy tree (0 = root)
 * @property children List of child ViewNodes
 * @property properties Additional view properties as key-value pairs
 */
data class ViewNode(
    val id: Int,
    val className: String,
    val simpleClassName: String,
    val resourceId: String?,
    val contentDescription: String?,
    val bounds: ViewBounds,
    val visibility: Int,
    val alpha: Float,
    val elevation: Float,
    val depth: Int,
    val children: List<ViewNode>,
    val properties: Map<String, String>,
) {
    /**
     * Returns the total count of this node and all descendants.
     */
    val totalCount: Int
        get() = 1 + children.sumOf { it.totalCount }

    /**
     * Returns whether this view is visible (visibility == View.VISIBLE).
     */
    val isVisible: Boolean
        get() = visibility == 0 // View.VISIBLE

    /**
     * Returns a display name for this view.
     * Prefers resource ID if available, then content description, then simple class name.
     */
    val displayName: String
        get() = resourceId?.substringAfterLast('/')
            ?: contentDescription
            ?: simpleClassName

    /**
     * Returns the view type category for color coding.
     */
    val viewType: ViewType
        get() = when {
            simpleClassName.contains("Layout", ignoreCase = true) ||
                simpleClassName.contains("ViewGroup", ignoreCase = true) ||
                simpleClassName.contains("Container", ignoreCase = true) ||
                simpleClassName.contains("Coordinator", ignoreCase = true) ||
                simpleClassName.contains("Frame", ignoreCase = true) ||
                simpleClassName.contains("Constraint", ignoreCase = true) ||
                simpleClassName.contains("Linear", ignoreCase = true) ||
                simpleClassName.contains("Relative", ignoreCase = true) -> ViewType.LAYOUT

            simpleClassName.contains("TextView", ignoreCase = true) ||
                simpleClassName.contains("EditText", ignoreCase = true) ||
                simpleClassName.contains("Text", ignoreCase = true) -> ViewType.TEXT

            simpleClassName.contains("Image", ignoreCase = true) -> ViewType.IMAGE

            simpleClassName.contains("Button", ignoreCase = true) ||
                simpleClassName.contains("Clickable", ignoreCase = true) ||
                simpleClassName.contains("Fab", ignoreCase = true) -> ViewType.BUTTON

            simpleClassName.contains("RecyclerView", ignoreCase = true) ||
                simpleClassName.contains("ListView", ignoreCase = true) ||
                simpleClassName.contains("ScrollView", ignoreCase = true) ||
                simpleClassName.contains("GridView", ignoreCase = true) ||
                simpleClassName.contains("Pager", ignoreCase = true) -> ViewType.LIST

            simpleClassName.contains("Checkbox", ignoreCase = true) ||
                simpleClassName.contains("RadioButton", ignoreCase = true) ||
                simpleClassName.contains("Switch", ignoreCase = true) ||
                simpleClassName.contains("Toggle", ignoreCase = true) -> ViewType.INPUT

            simpleClassName.contains("WebView", ignoreCase = true) -> ViewType.WEB

            simpleClassName.contains("Surface", ignoreCase = true) ||
                simpleClassName.contains("Texture", ignoreCase = true) -> ViewType.SURFACE

            else -> ViewType.OTHER
        }

    companion object {
        /**
         * Creates an empty ViewNode instance.
         */
        fun empty(): ViewNode = ViewNode(
            id = 0,
            className = "",
            simpleClassName = "",
            resourceId = null,
            contentDescription = null,
            bounds = ViewBounds.empty(),
            visibility = 0,
            alpha = 1f,
            elevation = 0f,
            depth = 0,
            children = emptyList(),
            properties = emptyMap(),
        )
    }
}

/**
 * Categorizes views for color coding in the hierarchy visualization.
 */
enum class ViewType {
    /** ViewGroups and layout containers */
    LAYOUT,
    /** TextViews and EditTexts */
    TEXT,
    /** ImageViews */
    IMAGE,
    /** Buttons and clickable elements */
    BUTTON,
    /** Lists, RecyclerViews, ScrollViews */
    LIST,
    /** Checkboxes, switches, radio buttons */
    INPUT,
    /** WebViews */
    WEB,
    /** SurfaceViews, TextureViews */
    SURFACE,
    /** Any other view type */
    OTHER,
}
