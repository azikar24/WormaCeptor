/*
 * Copyright AziKar24 2024.
 */

package com.azikar24.wormaceptorapp.sampleservice

/**
 * Constants for sample API endpoints used in testing content handling features.
 */
internal object SampleDataConstants {

    /**
     * PDF sample URLs.
     */
    object Pdf {
        /** Stata manual PDF sample. */
        const val STATA_MANUAL = "https://www.stata.com/manuals/dsample.pdf"
    }

    /**
     * Image sample URLs.
     */
    object Images {
        /** Random image from Picsum Photos. */
        const val PICSUM = "https://picsum.photos/400/300"

        /** PNG placeholder image. */
        const val PLACEHOLDER_PNG = "https://placehold.co/400x300/png"

        /** WebP format sample. */
        const val WEBP_GALLERY = "https://www.gstatic.com/webp/gallery/1.webp"

        /** Animated GIF sample. */
        const val GIPHY_GIF = "https://media.giphy.com/media/3o7TKsQ8MgBdI7tD3y/giphy.gif"
    }

    /**
     * JSON sample URLs.
     */
    object Json {
        /** JSONPlaceholder test endpoint. */
        const val PLACEHOLDER_POST = "https://jsonplaceholder.typicode.com/posts/1"
    }

    /**
     * XML sample URLs.
     */
    object Xml {
        /** W3Schools XML note sample. */
        const val W3SCHOOLS_NOTE = "https://www.w3schools.com/xml/note.xml"
    }

    /**
     * HTML sample URLs.
     */
    object Html {
        /** Basic example.com page. */
        const val EXAMPLE = "https://example.com"
    }
}
