package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorIndex: Int = 0,
    val category: String = "",
    val tags: String = ""
) {
    val tagList: List<String>
        get() = tags.split(",")
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
}
