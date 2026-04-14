package com.azikar24.wormaceptor.core.ui.components.button

/**
 * Visual variant for [WormaCeptorButton].
 */
enum class ButtonVariant {
    /** High-emphasis filled button for primary actions. */
    Primary,

    /** Medium-emphasis filled button with subtle background. */
    Secondary,

    /** Red-toned button for destructive actions (delete, clear). */
    Destructive,

    /** Button with border and no fill for secondary actions. */
    Outlined,

    /** Minimal button with no background for tertiary actions (cancel, dismiss). */
    Text,
}
