package com.azikar24.wormaceptorapp.sampleservice

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DemoDao {
    @Insert
    suspend fun insertTasks(tasks: List<DemoTaskEntity>)

    @Insert
    suspend fun insertNotes(notes: List<DemoNoteEntity>)

    @Query("UPDATE demo_task SET completed = :completed WHERE id = :id")
    suspend fun setTaskCompleted(
        id: Long,
        completed: Boolean,
    )

    @Query("DELETE FROM demo_note WHERE taskId = :taskId")
    suspend fun deleteNotesForTask(taskId: Long)

    @Query("SELECT * FROM demo_task ORDER BY priority DESC, createdAt DESC")
    suspend fun allTasks(): List<DemoTaskEntity>

    @Query("SELECT * FROM demo_note WHERE taskId = :taskId")
    suspend fun notesForTask(taskId: Long): List<DemoNoteEntity>
}
