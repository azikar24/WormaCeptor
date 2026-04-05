package com.azikar24.wormaceptor.feature.viewer.vm

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MVI ViewModel for the home screen shell (tab selection, overflow menu, tools search, category collapse).
 *
 * Manages home-screen-level UI state via [HomeViewState] that is independent
 * of the transaction or crash lists.
 */
class HomeViewModel(
    context: Context,
) : BaseViewModel<HomeViewState, HomeViewEffect, HomeViewEvent>(HomeViewState()) {

    private val collapsePrefs by lazy { context.getSharedPreferences(COLLAPSE_PREFS_NAME, Context.MODE_PRIVATE) }

    init {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                collapsePrefs.getStringSet(COLLAPSE_KEY, emptySet()) ?: emptySet()
            }
            if (saved.isNotEmpty()) {
                updateState { copy(collapsedToolCategories = saved) }
            }
        }
    }

    override fun handleEvent(event: HomeViewEvent) {
        when (event) {
            is HomeViewEvent.TabSelected -> updateState { copy(selectedTabIndex = event.index) }

            is HomeViewEvent.OverflowMenuVisibilityChanged ->
                updateState { copy(showOverflowMenu = event.visible) }

            is HomeViewEvent.ToolsSearchActiveChanged -> updateState {
                copy(toolsSearchActive = event.active, toolsSearchQuery = if (event.active) toolsSearchQuery else "")
            }

            is HomeViewEvent.ToolsSearchQueryChanged ->
                updateState { copy(toolsSearchQuery = event.query) }

            is HomeViewEvent.ToolCategoryCollapseToggled -> handleToolCategoryCollapseToggle(event.category)

            is HomeViewEvent.ShowMessage -> emitEffect(HomeViewEffect.ShowSnackBar(event.message))
        }
    }

    private fun handleToolCategoryCollapseToggle(category: String) {
        updateState {
            val updated = if (category in collapsedToolCategories) {
                collapsedToolCategories - category
            } else {
                collapsedToolCategories + category
            }
            copy(collapsedToolCategories = updated)
        }
        viewModelScope.launch(Dispatchers.IO) {
            collapsePrefs.edit {
                putStringSet(COLLAPSE_KEY, uiState.value.collapsedToolCategories)
            }
        }
    }

    companion object {
        private const val COLLAPSE_PREFS_NAME = "wormaceptor_tools_collapse"
        private const val COLLAPSE_KEY = "collapsed"
    }
}
