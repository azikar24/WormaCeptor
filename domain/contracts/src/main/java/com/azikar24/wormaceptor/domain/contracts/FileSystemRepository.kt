package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo

/**
 * Repository interface for accessing the app's file system.
 */
interface FileSystemRepository {

    /**
     * Lists all files and directories in the specified path.
     * @param path The absolute path to list files from
     * @return List of file entries sorted by directories first, then files alphabetically
     */
    suspend fun listFiles(path: String): List<FileEntry>

    /**
     * Reads the content of a file.
     * @param path The absolute path to the file
     * @return File content in appropriate format based on file type
     */
    suspend fun readFile(path: String): FileContent

    /**
     * Gets detailed information about a specific file.
     * @param path The absolute path to the file
     * @return Detailed file information
     */
    suspend fun getFileInfo(path: String): FileInfo

    /**
     * Gets the list of root directories accessible to the app.
     * @return List of root directory entries (filesDir, cacheDir, etc.)
     */
    suspend fun getRootDirectories(): List<FileEntry>

    /**
     * Deletes a file or empty directory.
     * @param path The absolute path to delete
     * @return True if deletion was successful, false otherwise
     */
    suspend fun deleteFile(path: String): Boolean
}
