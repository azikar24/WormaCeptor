package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.ExtensionContext
import com.azikar24.wormaceptor.domain.contracts.ExtensionProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for managing extension providers.
 *
 * Extension providers can be registered to extract custom metadata from network transactions.
 * The registry handles registration, unregistration, and extraction of extensions.
 */
interface ExtensionRegistry {
    /**
     * Register an extension provider.
     * If a provider with the same name already exists, it will be replaced.
     *
     * @param provider The extension provider to register
     */
    fun register(provider: ExtensionProvider)

    /**
     * Unregister an extension provider by name.
     *
     * @param name The name of the provider to unregister
     * @return true if a provider was removed, false if no provider with that name was found
     */
    fun unregister(name: String): Boolean

    /**
     * Extract extensions from all registered providers.
     * Providers are called in priority order (highest first).
     * If multiple providers return the same key, later providers will overwrite earlier values.
     *
     * @param context The transaction context to extract extensions from
     * @return Combined map of all extensions from all providers
     */
    fun extractAll(context: ExtensionContext): Map<String, String>

    /**
     * Get the names of all registered providers.
     *
     * @return List of provider names
     */
    fun getRegisteredProviders(): List<String>
}

/**
 * Default implementation of [ExtensionRegistry].
 * Thread-safe using ConcurrentHashMap.
 */
class DefaultExtensionRegistry : ExtensionRegistry {
    private val providers = ConcurrentHashMap<String, ExtensionProvider>()

    override fun register(provider: ExtensionProvider) {
        providers[provider.name] = provider
    }

    override fun unregister(name: String): Boolean {
        return providers.remove(name) != null
    }

    override fun extractAll(context: ExtensionContext): Map<String, String> {
        if (providers.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, String>()

        // Sort by priority (highest first) and extract
        providers.values
            .sortedByDescending { it.priority }
            .forEach { provider ->
                try {
                    val extensions = provider.extractExtensions(context)
                    result.putAll(extensions)
                } catch (e: Exception) {
                    // Log error but continue with other providers
                    // In production, consider using a proper logging framework
                    e.printStackTrace()
                }
            }

        return result
    }

    override fun getRegisteredProviders(): List<String> {
        return providers.keys.toList()
    }
}
