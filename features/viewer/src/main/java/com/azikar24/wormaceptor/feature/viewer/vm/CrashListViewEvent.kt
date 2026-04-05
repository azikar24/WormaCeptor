package com.azikar24.wormaceptor.feature.viewer.vm

/** All user-initiated actions dispatched from the crash list UI. */
sealed class CrashListViewEvent {
    /** User confirmed clearing all crashes. */
    data object ClearAllCrashes : CrashListViewEvent()

    /** User triggered pull-to-refresh on the crashes list. */
    data object RefreshCrashes : CrashListViewEvent()

    /**
     * Clear-crashes confirmation dialog visibility changed.
     *
     * @property visible Whether the dialog should be visible.
     */
    data class ClearCrashesDialogVisibilityChanged(val visible: Boolean) : CrashListViewEvent()

    /** User requested exporting all crashes. */
    data object ExportCrashesClicked : CrashListViewEvent()
}
