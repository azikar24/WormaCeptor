/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.data

import com.azikar24.wormaceptor.domain.contracts.FileSystemRepository
import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of FileSystemRepository using FileSystemDataSource.
 */
class FileSystemRepositoryImpl(
    private val dataSource: FileSystemDataSource,
) : FileSystemRepository {

    override suspend fun listFiles(path: String): List<FileEntry> = withContext(Dispatchers.IO) {
        dataSource.listFiles(path)
    }

    override suspend fun readFile(path: String): FileContent = withContext(Dispatchers.IO) {
        dataSource.readFile(path)
    }

    override suspend fun getFileInfo(path: String): FileInfo = withContext(Dispatchers.IO) {
        dataSource.getFileInfo(path)
    }

    override suspend fun getRootDirectories(): List<FileEntry> = withContext(Dispatchers.IO) {
        dataSource.getRootDirectories()
    }

    override suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        dataSource.deleteFile(path)
    }
}
