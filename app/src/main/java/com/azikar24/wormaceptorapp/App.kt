package com.azikar24.wormaceptorapp

import android.app.Application
import com.azikar24.wormaceptor.api.ExtensionContext
import com.azikar24.wormaceptor.api.Feature
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.domain.contracts.ExtensionProvider

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        WormaCeptorApi.init(
            context = this,
            logCrashes = true,
            features = Feature.ALL,
        )

        // Register test extension
        WormaCeptorApi.registerExtensionProvider(
            object : ExtensionProvider {
                override val name = "TestExtension"
                override fun extractExtensions(
                    context: ExtensionContext,
                ): Map<String, String> {
                    return mapOf(
                        "request_method" to context.request.method,
                        "has_response" to (context.response != null).toString(),
                        "custom_tag" to "test_value",
                    )
                }
            },
        )
    }
}
