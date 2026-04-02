package com.azikar24.wormaceptor.feature.dependenciesinspector.vm

import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo

/** User actions dispatched from the Dependencies Inspector UI. */
sealed class DependenciesInspectorViewEvent {
    data class SetSelectedCategory(val category: DependencyCategory?) : DependenciesInspectorViewEvent()
    data class SetSearchQuery(val query: String) : DependenciesInspectorViewEvent()
    data class SetShowVersionedOnly(val show: Boolean) : DependenciesInspectorViewEvent()
    data class SelectDependency(val dependency: DependencyInfo) : DependenciesInspectorViewEvent()
    data object DismissDetail : DependenciesInspectorViewEvent()
    data object Refresh : DependenciesInspectorViewEvent()
}
