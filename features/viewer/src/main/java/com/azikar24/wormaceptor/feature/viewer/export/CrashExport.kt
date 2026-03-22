package com.azikar24.wormaceptor.feature.viewer.export

import android.content.Context
import android.content.Intent
import com.azikar24.wormaceptor.domain.entities.Crash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Exports the given crashes as a JSON file and opens the system share sheet. */
suspend fun exportCrashes(
    context: Context,
    crashes: List<Crash>,
    onMessage: (String) -> Unit = {},
) {
    withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()

            crashes.forEach { crash ->
                val jsonObject = JSONObject().apply {
                    put("timestamp", crash.timestamp)
                    put("exceptionType", crash.exceptionType)
                    put("message", crash.message ?: "")
                    put("stackTrace", crash.stackTrace)
                }
                jsonArray.put(jsonObject)
            }

            val jsonContent = jsonArray.toString(2)

            withContext(Dispatchers.Main) {
                shareText(context, jsonContent, "WormaCeptor Crash Export", onMessage)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onMessage("Export failed: ${e.message}")
            }
        }
    }
}

private fun shareText(
    context: Context,
    content: String,
    subject: String,
    onMessage: (String) -> Unit,
) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        context.startActivity(Intent.createChooser(intent, "Export"))
    } catch (e: Exception) {
        onMessage("Failed to share: ${e.message}")
    }
}
