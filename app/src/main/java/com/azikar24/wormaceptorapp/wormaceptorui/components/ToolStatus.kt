package com.azikar24.wormaceptorapp.wormaceptorui.components

/**
 * Status for tool list items that show feedback.
 */
enum class ToolStatus {
    Idle,
    Running,
    Done,

    /** Waiting for user action such as rotating the screen. Shows hint message. */
    WaitingForAction,
}
