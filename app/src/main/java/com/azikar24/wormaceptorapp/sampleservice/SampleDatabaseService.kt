package com.azikar24.wormaceptorapp.sampleservice

import android.content.Context

object SampleDatabaseService {

    private const val HOUR_MS = 60L * 60L * 1000L
    private const val DAY_MS = 24L * HOUR_MS

    suspend fun seed(context: Context) {
        val dao = DemoRoomDatabase.get(context).demoDao()
        val now = System.currentTimeMillis()

        dao.insertTasks(
            listOf(
                DemoTaskEntity(
                    title = "Investigate FPS regression on Pixel 7",
                    priority = 3,
                    completed = false,
                    createdAt = now - HOUR_MS,
                ),
                DemoTaskEntity(
                    title = "Add HAR export to network module",
                    priority = 2,
                    completed = false,
                    createdAt = now - 2 * HOUR_MS,
                ),
                DemoTaskEntity(
                    title = "Verify mock-rules persistence",
                    priority = 1,
                    completed = true,
                    createdAt = now - DAY_MS,
                ),
                DemoTaskEntity(
                    title = "Write release notes",
                    priority = 1,
                    completed = false,
                    createdAt = now - 3 * HOUR_MS,
                ),
            ),
        )

        val tasks = dao.allTasks()
        if (tasks.isNotEmpty()) {
            dao.insertNotes(
                listOf(
                    DemoNoteEntity(taskId = tasks.first().id, body = "Reproduced under fast-scroll", updatedAt = now),
                    DemoNoteEntity(
                        taskId = tasks.first().id,
                        body = "Likely jank from image decode",
                        updatedAt = now - HOUR_MS,
                    ),
                    DemoNoteEntity(taskId = tasks.last().id, body = "Mention secure-storage parity", updatedAt = now),
                ),
            )
        }

        if (tasks.size >= 2) {
            dao.setTaskCompleted(tasks[1].id, true)
        }
        if (tasks.isNotEmpty()) {
            dao.deleteNotesForTask(tasks.last().id)
        }
    }
}
