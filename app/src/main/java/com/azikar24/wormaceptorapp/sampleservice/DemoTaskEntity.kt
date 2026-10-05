package com.azikar24.wormaceptorapp.sampleservice

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "demo_task")
data class DemoTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val priority: Int,
    val completed: Boolean,
    val createdAt: Long,
)
