package com.azikar24.wormaceptor.feature.preferences.data

import android.util.Log
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceSource
import java.io.File
import java.io.IOException

/**
 * Reads Preferences DataStore files from `files/datastore/`.
 *
 * Read-only: the host app's DataStore keeps its contents in memory and would overwrite or
 * ignore changes made to the file underneath it.
 *
 * @param dataStoreDirProvider Resolved on first use, off the main thread: `Context.getFilesDir()` hits the disk
 */
class DataStoreDataSource(dataStoreDirProvider: () -> File) {

    private val dataStoreDir by lazy(dataStoreDirProvider)

    /** Lists DataStore files by full file name, including the `.preferences_pb` extension. */
    fun getPreferenceFiles(): List<PreferenceFile> = dataStoreFiles().map { file ->
        PreferenceFile(
            name = file.name,
            itemCount = readItems(file).size,
            source = PreferenceSource.DATASTORE,
        )
    }

    /** Decoded key-value pairs of [fileName], or empty if it is missing or unreadable. */
    fun getPreferenceItems(fileName: String): List<PreferenceItem> = readItems(File(dataStoreDir, fileName))

    /** Modification time of [fileName], used to detect writes by the host app. */
    fun lastModified(fileName: String): Long = File(dataStoreDir, fileName).lastModified()

    private fun dataStoreFiles(): List<File> = dataStoreDir.listFiles()
        ?.filter { it.isFile && it.name.endsWith(FILE_SUFFIX) }
        .orEmpty()

    private fun readItems(file: File): List<PreferenceItem> = try {
        if (file.isFile) PreferencesProtoDecoder.decode(file.readBytes()) else emptyList()
    } catch (e: IOException) {
        Log.w(TAG, "Could not read ${file.name}", e)
        emptyList()
    } catch (e: IllegalArgumentException) {
        Log.w(TAG, "Could not decode ${file.name}", e)
        emptyList()
    }

    private companion object {
        const val TAG = "DataStoreDataSource"
        const val FILE_SUFFIX = ".preferences_pb"
    }
}
