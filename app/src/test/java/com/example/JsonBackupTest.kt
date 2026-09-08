package com.example

import com.example.data.backup.JsonBackupHelper
import com.example.data.local.CommentEntity
import com.example.data.local.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class JsonBackupTest {

    @Test
    fun exportAndParseAllNotes_roundTripSucceeds() {
        val notes = listOf(
            NoteEntity(
                id = 1,
                title = "Ghi chú họp",
                description = "Nội dung họp dự án",
                isPinned = true,
                category = "Công việc",
                tags = "hop, duan"
            ),
            NoteEntity(
                id = 2,
                title = "Danh sách việc cần làm",
                description = "Mua sắm, dọn dẹp",
                isPinned = false,
                category = "Cá nhân",
                tags = "todo, giadinh"
            )
        )
        val comments = listOf(
            CommentEntity(id = 10, noteId = 1, parentId = null, authorName = "Nguyễn Văn A", content = "Bình luận gốc"),
            CommentEntity(id = 11, noteId = 1, parentId = 10, authorName = "Trần Thị B", replyToAuthor = "Nguyễn Văn A", content = "Phản hồi cho A")
        )

        val jsonString = JsonBackupHelper.exportAllDataToJson(notes, comments)
        assertTrue(jsonString.contains("Ghi chú họp"))
        assertTrue(jsonString.contains("Bình luận gốc"))
        assertTrue(jsonString.contains("Phản hồi cho A"))

        val parseResult = JsonBackupHelper.parseJson(jsonString)
        assertTrue(parseResult.isSuccess)

        val backupData = parseResult.getOrThrow()
        assertEquals(2, backupData.notes.size)
        assertEquals(2, backupData.notes[0].comments.size)
        assertEquals(2, backupData.totalCommentsCount)

        val note1 = backupData.notes[0]
        assertEquals("Ghi chú họp", note1.title)
        assertEquals("Công việc", note1.category)
        assertEquals("hop, duan", note1.tags)
        assertEquals(2, note1.comments.size)
        assertEquals("Nguyễn Văn A", note1.comments[0].authorName)
        assertEquals("Trần Thị B", note1.comments[1].authorName)
        assertEquals(10L, note1.comments[1].parentId)

        val note2 = backupData.notes[1]
        assertEquals("Danh sách việc cần làm", note2.title)
        assertEquals("Cá nhân", note2.category)
        assertEquals("todo, giadinh", note2.tags)
    }

    @Test
    fun exportAndParseSingleNote_succeeds() {
        val note = NoteEntity(id = 5, title = "Ý tưởng kinh doanh", description = "Mô hình mới", isPinned = false)
        val comments = listOf(
            CommentEntity(id = 21, noteId = 5, parentId = null, authorName = "Thành", content = "Ý tưởng hay!"),
            CommentEntity(id = 22, noteId = 5, parentId = 21, authorName = "Hương", content = "Tôi cũng nghĩ vậy.")
        )

        val jsonString = JsonBackupHelper.exportSingleNoteToJson(note, comments)
        assertTrue(jsonString.contains("Ý tưởng kinh doanh"))
        assertTrue(jsonString.contains("Ý tưởng hay!"))

        val parseResult = JsonBackupHelper.parseJson(jsonString)
        assertTrue(parseResult.isSuccess)

        val data = parseResult.getOrThrow()
        assertEquals(1, data.notes.size)
        assertEquals("Ý tưởng kinh doanh", data.notes[0].title)
        assertEquals(2, data.notes[0].comments.size)
    }

    @Test
    fun parseInvalidJson_returnsFailureGracefully() {
        val invalidJson = "This is not json"
        val parseResult = JsonBackupHelper.parseJson(invalidJson)
        assertTrue(parseResult.isFailure)
        assertNotNull(parseResult.exceptionOrNull())
    }
}
