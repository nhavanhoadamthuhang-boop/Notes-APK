package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE noteId = :noteId ORDER BY createdAt ASC")
    fun getCommentsForNote(noteId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE noteId = :noteId ORDER BY createdAt ASC")
    suspend fun getCommentsForNoteDirect(noteId: Long): List<CommentEntity>

    @Query("SELECT * FROM comments ORDER BY createdAt ASC")
    fun getAllComments(): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments ORDER BY id ASC")
    suspend fun getAllCommentsDirect(): List<CommentEntity>

    @Query("SELECT COUNT(*) FROM comments WHERE noteId = :noteId")
    fun getCommentCount(noteId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM comments WHERE noteId = :noteId")
    suspend fun getCommentCountDirect(noteId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>): List<Long>

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Query("UPDATE comments SET content = :content, authorName = :authorName WHERE id = :id")
    suspend fun updateCommentContentAndAuthor(id: Long, content: String, authorName: String)

    @Query("UPDATE comments SET isPinned = :isPinned WHERE id = :id")
    suspend fun updateCommentPinned(id: Long, isPinned: Boolean)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :id OR parentId = :id")
    suspend fun deleteCommentAndReplies(id: Long)

    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()
}
