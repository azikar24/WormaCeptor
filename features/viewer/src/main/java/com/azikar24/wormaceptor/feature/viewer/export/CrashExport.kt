package com.azikar24.wormaceptor.feature.viewer.export

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.domain.entities.Crash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

suspend fun exportCrashes(context: Context, crashes: List<Crash>) {
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
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "wormaceptor_crashes_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(jsonArray.toString(2))
            
            withContext(Dispatchers.Main) {
                shareFile(context, file, "WormaCeptor Crash Export")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

private fun shareFile(context: Context, file: File, subject: String) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share file: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
