package com.example

import com.example.data.local.NoteEntity
import com.example.ui.NoteSortOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteSortTest {

    @Test
    fun sortNotes_newestFirst_ordersByCreatedAtDescending() {
        val note1 = NoteEntity(id = 1, title = "First Note", description = "Desc 1", createdAt = 1000L)
        val note2 = NoteEntity(id = 2, title = "Second Note", description = "Desc 2", createdAt = 3000L)
        val note3 = NoteEntity(id = 3, title = "Third Note", description = "Desc 3", createdAt = 2000L)

        val list = listOf(note1, note2, note3)
        val sorted = when (NoteSortOrder.NEWEST_FIRST) {
            NoteSortOrder.NEWEST_FIRST -> list.sortedByDescending { it.createdAt }
            NoteSortOrder.OLDEST_FIRST -> list.sortedBy { it.createdAt }
        }

        assertEquals(listOf(note2, note3, note1), sorted)
        assertEquals(3000L, sorted[0].createdAt)
        assertEquals(2000L, sorted[1].createdAt)
        assertEquals(1000L, sorted[2].createdAt)
    }

    @Test
    fun sortNotes_oldestFirst_ordersByCreatedAtAscending() {
        val note1 = NoteEntity(id = 1, title = "First Note", description = "Desc 1", createdAt = 1000L)
        val note2 = NoteEntity(id = 2, title = "Second Note", description = "Desc 2", createdAt = 3000L)
        val note3 = NoteEntity(id = 3, title = "Third Note", description = "Desc 3", createdAt = 2000L)

        val list = listOf(note1, note2, note3)
        val sorted = when (NoteSortOrder.OLDEST_FIRST) {
            NoteSortOrder.NEWEST_FIRST -> list.sortedByDescending { it.createdAt }
            NoteSortOrder.OLDEST_FIRST -> list.sortedBy { it.createdAt }
        }

        assertEquals(listOf(note1, note3, note2), sorted)
        assertEquals(1000L, sorted[0].createdAt)
        assertEquals(2000L, sorted[1].createdAt)
        assertEquals(3000L, sorted[2].createdAt)
    }
}
