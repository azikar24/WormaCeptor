package com.azikar24.wormaceptor.feature.viewer.data

import android.content.Context
import android.content.SharedPreferences
import com.azikar24.wormaceptor.api.Feature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing favorite tools stored in SharedPreferences.
 */
class FavoritesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _favorites = MutableStateFlow<Set<Feature>>(loadFavorites())
    val favorites: StateFlow<Set<Feature>> = _favorites.asStateFlow()

    /**
     * Add a feature to favorites.
     * Enforces maximum of [MAX_FAVORITES] items.
     * @return true if added successfully, false if already at max capacity
     */
    fun addFavorite(feature: Feature): Boolean {
        val current = _favorites.value
        if (current.size >= MAX_FAVORITES) {
            return false
        }
        if (feature in current) {
            return true
        }
        val updated = current + feature
        saveFavorites(updated)
        _favorites.value = updated
        return true
    }

    /**
     * Remove a feature from favorites.
     */
    fun removeFavorite(feature: Feature) {
        val current = _favorites.value
        if (feature !in current) return
        val updated = current - feature
        saveFavorites(updated)
        _favorites.value = updated
    }

    /**
     * Toggle a feature's favorite status.
     * @return true if the feature is now a favorite, false otherwise
     */
    fun toggleFavorite(feature: Feature): Boolean {
        return if (feature in _favorites.value) {
            removeFavorite(feature)
            false
        } else {
            addFavorite(feature)
        }
    }

    /**
     * Check if a feature is a favorite.
     */
    fun isFavorite(feature: Feature): Boolean = feature in _favorites.value

    /**
     * Get the current number of favorites.
     */
    fun getFavoritesCount(): Int = _favorites.value.size

    /**
     * Clear all favorites.
     */
    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
        _favorites.value = emptySet()
    }

    /**
     * Set default favorites for new users.
     * Only applies if no favorites have been set previously.
     */
    fun setDefaultsIfNeeded() {
        if (!prefs.contains(KEY_FAVORITES)) {
            saveFavorites(DEFAULT_FAVORITES)
            _favorites.value = DEFAULT_FAVORITES
        }
    }

    private fun loadFavorites(): Set<Feature> {
        val savedNames = prefs.getStringSet(KEY_FAVORITES, null)
        return savedNames?.mapNotNull { name ->
            try {
                Feature.valueOf(name)
            } catch (e: IllegalArgumentException) {
                null
            }
        }?.toSet() ?: emptySet()
    }

    private fun saveFavorites(features: Set<Feature>) {
        val names = features.map { it.name }.toSet()
        prefs.edit().putStringSet(KEY_FAVORITES, names).apply()
    }

    companion object {
        private const val PREFS_NAME = "wormaceptor_favorites"
        private const val KEY_FAVORITES = "favorite_tools"

        /** Maximum number of favorite tools allowed */
        const val MAX_FAVORITES = 8

        /** Default favorites for new users */
        val DEFAULT_FAVORITES: Set<Feature> = setOf(
            Feature.CONSOLE_LOGS,
            Feature.SHARED_PREFERENCES,
            Feature.MEMORY_MONITOR,
            Feature.VIEW_HIERARCHY
        )

        @Volatile
        private var instance: FavoritesRepository? = null

        fun getInstance(context: Context): FavoritesRepository {
            return instance ?: synchronized(this) {
                instance ?: FavoritesRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
