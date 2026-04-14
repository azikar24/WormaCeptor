package com.azikar24.wormaceptor.core.ui.components.card

/**
 * Visual style for [WormaCeptorCard].
 */
enum class CardStyle {
    /** Surface card with subtle elevation-based background. Default for most cards. */
    Filled,

    /** Card with a thin border and subtle fill. For selectable or expandable content. */
    Outlined,

    /** Card with visible elevation shadow. For prominent/floating cards. */
    Elevated,

    /**
     * Signature "accent" card using the asymmetric shape -- one large rounded
     * corner, three small. Pair with [WormaCeptorCard]'s `accentStripe` param
     * for a recognizable pro-tool voice.
     */
    Accent,
}
