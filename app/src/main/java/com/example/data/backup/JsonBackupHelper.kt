package com.example.data.backup

import com.example.data.local.CommentEntity
import com.example.data.local.NoteEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupComment(
    val id: Long? = null,
    val parentId: Long? = null,
    val authorName: String,
    val replyToAuthor: String? = null,
    val content: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class BackupNote(
    val id: Long? = null,
    val title: String,
    val description: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorIndex: Int = 0,
    val category: String = "",
    val tags: String = "",
    val comments: List<BackupComment> = emptyList()
)

data class BackupData(
    val version: Int = 1,
    val appName: String = "Ghi Chú",
    val exportedAt: Long = System.currentTimeMillis(),
    val notes: List<BackupNote> = emptyList()
) {
    val totalCommentsCount: Int
        get() = notes.sumOf { it.comments.size }
}

object JsonBackupHelper {

    fun exportAllDataToJson(notes: List<NoteEntity>, comments: List<CommentEntity>): String {
        val rootObj = JSONObject()
        rootObj.put("version", 1)
        rootObj.put("appName", "Ghi Chú")
        rootObj.put("exportedAt", System.currentTimeMillis())
        rootObj.put("noteCount", notes.size)
        rootObj.put("totalCommentCount", comments.size)

        val commentsByNoteId = comments.groupBy { it.noteId }

        val notesArray = JSONArray()
        for (note in notes) {
            val noteObj = JSONObject()
            noteObj.put("id", note.id)
            noteObj.put("title", note.title)
            noteObj.put("description", note.description)
            noteObj.put("isPinned", note.isPinned)
            noteObj.put("createdAt", note.createdAt)
            noteObj.put("updatedAt", note.updatedAt)
            noteObj.put("colorIndex", note.colorIndex)
            noteObj.put("category", note.category)
            noteObj.put("tags", note.tags)

            val noteComments = commentsByNoteId[note.id] ?: emptyList()
            val commentsArray = JSONArray()
            for (comment in noteComments) {
                val commentObj = JSONObject()
                commentObj.put("id", comment.id)
                if (comment.parentId != null) {
                    commentObj.put("parentId", comment.parentId)
                } else {
                    commentObj.put("parentId", JSONObject.NULL)
                }
                commentObj.put("authorName", comment.authorName)
                if (comment.replyToAuthor != null) {
                    commentObj.put("replyToAuthor", comment.replyToAuthor)
                } else {
                    commentObj.put("replyToAuthor", JSONObject.NULL)
                }
                commentObj.put("content", comment.content)
                commentObj.put("isPinned", comment.isPinned)
                commentObj.put("createdAt", comment.createdAt)

                commentsArray.put(commentObj)
            }
            noteObj.put("comments", commentsArray)
            notesArray.put(noteObj)
        }

        rootObj.put("notes", notesArray)
        return rootObj.toString(2)
    }

    fun exportSingleNoteToJson(note: NoteEntity, comments: List<CommentEntity>): String {
        val rootObj = JSONObject()
        rootObj.put("version", 1)
        rootObj.put("appName", "Ghi Chú")
        rootObj.put("exportedAt", System.currentTimeMillis())
        rootObj.put("noteCount", 1)
        rootObj.put("totalCommentCount", comments.size)

        val notesArray = JSONArray()
        val noteObj = JSONObject()
        noteObj.put("id", note.id)
        noteObj.put("title", note.title)
        noteObj.put("description", note.description)
        noteObj.put("isPinned", note.isPinned)
        noteObj.put("createdAt", note.createdAt)
        noteObj.put("updatedAt", note.updatedAt)
        noteObj.put("colorIndex", note.colorIndex)
        noteObj.put("category", note.category)
        noteObj.put("tags", note.tags)

        val commentsArray = JSONArray()
        for (comment in comments) {
            val commentObj = JSONObject()
            commentObj.put("id", comment.id)
            if (comment.parentId != null) {
                commentObj.put("parentId", comment.parentId)
            } else {
                commentObj.put("parentId", JSONObject.NULL)
            }
            commentObj.put("authorName", comment.authorName)
            if (comment.replyToAuthor != null) {
                commentObj.put("replyToAuthor", comment.replyToAuthor)
            } else {
                commentObj.put("replyToAuthor", JSONObject.NULL)
            }
            commentObj.put("content", comment.content)
            commentObj.put("isPinned", comment.isPinned)
            commentObj.put("createdAt", comment.createdAt)

            commentsArray.put(commentObj)
        }
        noteObj.put("comments", commentsArray)
        notesArray.put(noteObj)

        rootObj.put("notes", notesArray)
        return rootObj.toString(2)
    }

    fun parseJson(jsonString: String): Result<BackupData> {
        return try {
            val trimmed = jsonString.trim()
            if (trimmed.isEmpty()) {
                return Result.failure(IllegalArgumentException("Tệp tin JSON trống, vui lòng kiểm tra lại."))
            }

            if (trimmed.startsWith("[")) {
                // Array of notes directly
                val array = JSONArray(trimmed)
                val notes = parseNotesArray(array)
                return Result.success(BackupData(notes = notes))
            }

            val rootObj = JSONObject(trimmed)
            val version = rootObj.optInt("version", 1)
            val appName = rootObj.optString("appName", "Ghi Chú")
            val exportedAt = rootObj.optLong("exportedAt", System.currentTimeMillis())

            val parsedNotes = mutableListOf<BackupNote>()

            if (rootObj.has("notes")) {
                val notesArray = rootObj.getJSONArray("notes")
                parsedNotes.addAll(parseNotesArray(notesArray))
            } else if (rootObj.has("title")) {
                // Single note object directly
                parsedNotes.add(parseSingleNoteObject(rootObj))
            } else if (rootObj.has("note")) {
                val noteObj = rootObj.getJSONObject("note")
                val commentsArray = rootObj.optJSONArray("comments") ?: noteObj.optJSONArray("comments")
                parsedNotes.add(parseSingleNoteObject(noteObj, commentsArray))
            }

            // If comments were in a separate top-level array linked by noteId
            if (rootObj.has("comments") && !rootObj.has("notes")) {
                // Already handled in single note case above
            }

            if (parsedNotes.isEmpty()) {
                return Result.failure(IllegalArgumentException("Không tìm thấy ghi chú nào hợp lệ trong tệp .json."))
            }

            Result.success(
                BackupData(
                    version = version,
                    appName = appName,
                    exportedAt = exportedAt,
                    notes = parsedNotes
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Định dạng tệp JSON không hợp lệ: ${e.message}", e))
        }
    }

    private fun parseNotesArray(array: JSONArray): List<BackupNote> {
        val list = mutableListOf<BackupNote>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            list.add(parseSingleNoteObject(obj))
        }
        return list
    }

    private fun parseSingleNoteObject(obj: JSONObject, externalCommentsArray: JSONArray? = null): BackupNote {
        val id = if (obj.has("id") && !obj.isNull("id")) obj.getLong("id") else null
        val title = obj.optString("title", "Ghi chú không tiêu đề")
        val description = obj.optString("description", "")
        val isPinned = obj.optBoolean("isPinned", false)
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = obj.optLong("updatedAt", createdAt)
        val colorIndex = obj.optInt("colorIndex", 0)
        val category = obj.optString("category", "")
        val tags = obj.optString("tags", "")

        val commentsArray = externalCommentsArray ?: obj.optJSONArray("comments")
        val comments = mutableListOf<BackupComment>()
        if (commentsArray != null) {
            for (j in 0 until commentsArray.length()) {
                val commentObj = commentsArray.optJSONObject(j) ?: continue
                val commentId = if (commentObj.has("id") && !commentObj.isNull("id")) commentObj.getLong("id") else null
                val parentId = if (commentObj.has("parentId") && !commentObj.isNull("parentId")) commentObj.getLong("parentId") else null
                val authorName = commentObj.optString("authorName", "Người dùng").ifBlank { "Người dùng" }
                val replyToAuthor = if (commentObj.has("replyToAuthor") && !commentObj.isNull("replyToAuthor")) {
                    commentObj.getString("replyToAuthor").ifBlank { null }
                } else null
                val content = commentObj.optString("content", "").trim()
                val isCommentPinned = commentObj.optBoolean("isPinned", false)
                val commentCreatedAt = commentObj.optLong("createdAt", System.currentTimeMillis())

                if (content.isNotBlank()) {
                    comments.add(
                        BackupComment(
                            id = commentId,
                            parentId = parentId,
                            authorName = authorName,
                            replyToAuthor = replyToAuthor,
                            content = content,
                            isPinned = isCommentPinned,
                            createdAt = commentCreatedAt
                        )
                    )
                }
            }
        }

        return BackupNote(
            id = id,
            title = title,
            description = description,
            isPinned = isPinned,
            createdAt = createdAt,
            updatedAt = updatedAt,
            colorIndex = colorIndex,
            category = category,
            tags = tags,
            comments = comments
        )
    }
}
