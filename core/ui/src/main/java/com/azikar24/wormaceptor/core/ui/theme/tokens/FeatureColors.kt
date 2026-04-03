@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")

package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.ui.graphics.Color

/**
 * Domain-agnostic color groups shared across features and UI domains.
 * Every hex value references [Palette] -- no hardcoded literals allowed.
 */
object Colors {

    // ================================================================
    // STATUS
    // ================================================================

    /** Semantic status indicator colors used across all features. */
    object Status {
        val green = Palette.Emerald500
        val amber = Palette.Amber500
        val orange = Palette.Orange500
        val red = Palette.Red500
        val blue = Palette.Blue700
        val grey = Palette.Gray625
    }

    // ================================================================
    // CHART
    // ================================================================

    /** Colors for data visualisation and chart elements. */
    object Chart {
        val fast = Palette.Emerald500
        val medium = Palette.Amber500
        val slow = Palette.Red500
        val success2xx = Palette.Emerald500
        val redirect3xx = Palette.Blue700
        val clientError4xx = Palette.Amber500
        val serverError5xx = Palette.Red500
    }

    // ================================================================
    // CATEGORY
    // ================================================================

    /** Colors for tool category groupings in the tools grid. */
    object Category {
        val inspection = Palette.Indigo600
        val performance = Palette.Amber500
        val network = Palette.Emerald500
        val simulation = Palette.Purple600
        val core = Palette.Blue700
        val favorites = Palette.Amber500
        val fallback = Palette.Gray625
    }

    // ================================================================
    // CONTENT TYPE
    // ================================================================

    /** Colors for HTTP content type indicators. */
    object ContentType {
        val json = Palette.Amber500
        val xml = Palette.Purple600
        val html = Palette.Pink500
        val protobuf = Palette.Emerald500
        val formData = Palette.Blue700
        val multipart = Palette.Indigo600
        val plainText = Palette.Gray625
        val binary = Palette.Red500
        val pdf = Palette.Red600
        val image = Palette.Teal500
        val unknown = Palette.Gray400
    }

    // ================================================================
    // HTTP METHOD
    // ================================================================

    /** Colors for HTTP method indicators. */
    object HttpMethod {
        val get = Palette.Blue700
        val post = Palette.Emerald500
        val put = Palette.Amber500
        val patch = Palette.Purple600
        val delete = Palette.Red500
        val head = Palette.Gray625
        val options = Palette.Indigo600
        val connect = Palette.Teal500
        val trace = Palette.Gray400

        /** Returns the color for the given HTTP method name. */
        fun forMethod(method: String): Color = when (method.uppercase()) {
            "GET" -> get
            "POST" -> post
            "PUT" -> put
            "PATCH" -> patch
            "DELETE" -> delete
            "HEAD" -> head
            "OPTIONS" -> options
            "CONNECT" -> connect
            "TRACE" -> trace
            else -> get
        }
    }

    // ================================================================
    // ACCENT
    // ================================================================

    /** Accent colors for highlights and emphasis. */
    object Accent {
        val tertiary = Palette.Teal500
        val highlight = Palette.AmberHighlight
    }
}
