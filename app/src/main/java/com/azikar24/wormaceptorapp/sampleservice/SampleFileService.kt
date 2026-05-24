package com.azikar24.wormaceptorapp.sampleservice

import android.content.Context
import java.io.File

object SampleFileService {

    private const val BYTE_MASK = 256
    private const val SAMPLE_BIN_SIZE = 4 * 1024
    private const val SAMPLE_JSON =
        """{"name":"Aziz","theme":"dark","beta":true,"prefs":{"density":"compact","tabs":["network","memory"]}}"""
    private const val SAMPLE_TEXT = "Quick scratch notes used by the demo file browser.\nLine 2.\nLine 3.\n"
    private const val SAMPLE_MARKDOWN = "# Demo changelog\n\n- Added storage demo seeders\n- Added crypto sample\n"
    private const val SAMPLE_LOG = "00:00 boot\n00:01 init\n00:02 ready\n"

    fun seed(context: Context) {
        val ctx = context.applicationContext
        val files = ctx.filesDir
        val cache = ctx.cacheDir

        File(files, "demo").mkdirs()
        File(files, "demo/profile.json").writeText(SAMPLE_JSON)
        File(files, "demo/notes.txt").writeText(SAMPLE_TEXT)
        File(files, "demo/changelog.md").writeText(SAMPLE_MARKDOWN)

        File(cache, "scratch").mkdirs()
        File(cache, "scratch/payload.bin").writeBytes(ByteArray(SAMPLE_BIN_SIZE) { (it % BYTE_MASK).toByte() })
        File(cache, "scratch/transient.log").writeText(SAMPLE_LOG)
    }
}
