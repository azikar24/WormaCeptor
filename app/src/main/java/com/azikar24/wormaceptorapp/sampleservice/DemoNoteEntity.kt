package com.azikar24.wormaceptorapp.sampleservice

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "demo_note")
data class DemoNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val body: String,
    val updatedAt: Long,
)
