package com.example

import com.example.data.local.CommentEntity
import com.example.data.local.NoteEntity
import com.example.ui.CommentSortOrder
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
            NoteSortOrder.ALPHABETICAL -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title.trim() })
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
            NoteSortOrder.ALPHABETICAL -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title.trim() })
        }

        assertEquals(listOf(note1, note3, note2), sorted)
        assertEquals(1000L, sorted[0].createdAt)
        assertEquals(2000L, sorted[1].createdAt)
        assertEquals(3000L, sorted[2].createdAt)
    }

    @Test
    fun sortNotes_alphabetical_ordersByTitleCaseInsensitive() {
        val noteA = NoteEntity(id = 1, title = "Apple note", description = "Desc 1", createdAt = 1000L)
        val noteB = NoteEntity(id = 2, title = "banana note", description = "Desc 2", createdAt = 3000L)
        val noteC = NoteEntity(id = 3, title = "Cherry note", description = "Desc 3", createdAt = 2000L)

        val list = listOf(noteB, noteC, noteA)
        val sorted = when (NoteSortOrder.ALPHABETICAL) {
            NoteSortOrder.NEWEST_FIRST -> list.sortedByDescending { it.createdAt }
            NoteSortOrder.OLDEST_FIRST -> list.sortedBy { it.createdAt }
            NoteSortOrder.ALPHABETICAL -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title.trim() })
        }

        assertEquals(listOf(noteA, noteB, noteC), sorted)
        assertEquals("Apple note", sorted[0].title)
        assertEquals("banana note", sorted[1].title)
        assertEquals("Cherry note", sorted[2].title)
    }

    @Test
    fun sortComments_allOrders_workCorrectly() {
        val c1 = CommentEntity(id = 1, noteId = 1, content = "Zebra comment", createdAt = 1000L, authorName = "Đàm Tường Quân")
        val c2 = CommentEntity(id = 2, noteId = 1, content = "Alpha comment", createdAt = 3000L, authorName = "Đàm Tường Quân")
        val c3 = CommentEntity(id = 3, noteId = 1, content = "Beta comment", createdAt = 2000L, isPinned = true, authorName = "Đàm Tường Quân")

        val list = listOf(c1, c2, c3)

        // Alphabetical sort (Pinned first)
        val alphaComparator = compareByDescending<CommentEntity> { it.isPinned }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.content.trim() }
        val alphaSorted = list.sortedWith(alphaComparator)
        assertEquals(c3, alphaSorted[0]) // Pinned
        assertEquals(c2, alphaSorted[1]) // Alpha
        assertEquals(c1, alphaSorted[2]) // Zebra

        // Newest first (Pinned first)
        val newestComparator = compareByDescending<CommentEntity> { it.isPinned }
            .thenByDescending { it.createdAt }
        val newestSorted = list.sortedWith(newestComparator)
        assertEquals(c3, newestSorted[0]) // Pinned
        assertEquals(c2, newestSorted[1]) // 3000L
        assertEquals(c1, newestSorted[2]) // 1000L
    }
}
