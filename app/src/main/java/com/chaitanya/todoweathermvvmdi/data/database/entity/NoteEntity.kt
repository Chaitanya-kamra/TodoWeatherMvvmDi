package com.chaitanya.todoweathermvvmdi.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes-table")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val isPriority: Boolean = false,
    val isCompleted: Boolean = false
)
