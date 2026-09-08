package com.example.data.repository

import com.example.data.backup.BackupData
import com.example.data.local.CommentDao
import com.example.data.local.CommentEntity
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

data class ImportSummary(
    val notesImported: Int,
    val commentsImported: Int,
    val repliesImported: Int
)

class NoteRepository(
    private val noteDao: NoteDao,
    private val commentDao: CommentDao
) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allComments: Flow<List<CommentEntity>> = commentDao.getAllComments()

    fun getNote(id: Long): Flow<NoteEntity?> = noteDao.getNoteById(id)

    fun getComments(noteId: Long): Flow<List<CommentEntity>> = commentDao.getCommentsForNote(noteId)

    fun getCommentCount(noteId: Long): Flow<Int> = commentDao.getCommentCount(noteId)

    suspend fun getAllNotesDirect(): List<NoteEntity> = noteDao.getAllNotesDirect()

    suspend fun getAllCommentsDirect(): List<CommentEntity> = commentDao.getAllCommentsDirect()

    suspend fun getNoteByIdDirect(id: Long): NoteEntity? = noteDao.getNoteByIdDirect(id)

    suspend fun getCommentsForNoteDirect(noteId: Long): List<CommentEntity> = commentDao.getCommentsForNoteDirect(noteId)

    suspend fun insertNote(
        title: String,
        description: String,
        isPinned: Boolean = false,
        category: String = "",
        tags: String = ""
    ): Long {
        val note = NoteEntity(
            title = title.trim(),
            description = description.trim(),
            isPinned = isPinned,
            category = category.trim(),
            tags = tags.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleNotePinned(id: Long, currentPinned: Boolean) {
        noteDao.updateNotePinned(id, !currentPinned)
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun insertComment(
        noteId: Long,
        content: String,
        authorName: String = "Người dùng",
        parentId: Long? = null,
        replyToAuthor: String? = null
    ): Long {
        val comment = CommentEntity(
            noteId = noteId,
            parentId = parentId,
            authorName = authorName.ifBlank { "Người dùng" },
            replyToAuthor = replyToAuthor,
            content = content.trim(),
            isPinned = false,
            createdAt = System.currentTimeMillis()
        )
        return commentDao.insertComment(comment)
    }

    suspend fun toggleCommentPinned(id: Long, currentPinned: Boolean) {
        commentDao.updateCommentPinned(id, !currentPinned)
    }

    suspend fun updateComment(id: Long, content: String, authorName: String) {
        commentDao.updateCommentContentAndAuthor(
            id = id,
            content = content.trim(),
            authorName = authorName.ifBlank { "Bạn" }
        )
    }

    suspend fun deleteComment(id: Long) {
        commentDao.deleteCommentAndReplies(id)
    }

    suspend fun importBackupData(backupData: BackupData, replaceExisting: Boolean): ImportSummary {
        if (replaceExisting) {
            commentDao.deleteAllComments()
            noteDao.deleteAllNotes()
        }

        var importedNotesCount = 0
        var importedCommentsCount = 0
        var importedRepliesCount = 0

        for (backupNote in backupData.notes) {
            val noteEntity = NoteEntity(
                title = backupNote.title.trim().ifBlank { "Ghi chú đã nhập" },
                description = backupNote.description.trim(),
                isPinned = backupNote.isPinned,
                createdAt = backupNote.createdAt,
                updatedAt = backupNote.updatedAt,
                colorIndex = backupNote.colorIndex,
                category = backupNote.category.trim(),
                tags = backupNote.tags.trim()
            )
            val newNoteId = noteDao.insertNote(noteEntity)
            importedNotesCount++

            // Split comments into root comments and replies
            val rootComments = backupNote.comments.filter { it.parentId == null }
            val replyComments = backupNote.comments.filter { it.parentId != null }

            val oldToNewCommentIdMap = mutableMapOf<Long, Long>()

            // 1. Insert root comments
            for (rootComment in rootComments) {
                val commentEntity = CommentEntity(
                    noteId = newNoteId,
                    parentId = null,
                    authorName = rootComment.authorName.ifBlank { "Người dùng" },
                    replyToAuthor = rootComment.replyToAuthor,
                    content = rootComment.content.trim(),
                    isPinned = rootComment.isPinned,
                    createdAt = rootComment.createdAt
                )
                val newCommentId = commentDao.insertComment(commentEntity)
                importedCommentsCount++
                if (rootComment.id != null) {
                    oldToNewCommentIdMap[rootComment.id] = newCommentId
                }
            }

            // 2. Insert replies
            for (reply in replyComments) {
                val remappedParentId = reply.parentId?.let { oldToNewCommentIdMap[it] }
                val commentEntity = CommentEntity(
                    noteId = newNoteId,
                    parentId = remappedParentId, // If parent found in map, link properly; otherwise attaches to note
                    authorName = reply.authorName.ifBlank { "Người dùng" },
                    replyToAuthor = reply.replyToAuthor,
                    content = reply.content.trim(),
                    isPinned = reply.isPinned,
                    createdAt = reply.createdAt
                )
                val newReplyId = commentDao.insertComment(commentEntity)
                importedRepliesCount++
                if (reply.id != null) {
                    oldToNewCommentIdMap[reply.id] = newReplyId
                }
            }
        }

        return ImportSummary(
            notesImported = importedNotesCount,
            commentsImported = importedCommentsCount,
            repliesImported = importedRepliesCount
        )
    }
}
