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
    @Query("SELECT * FROM comments WHERE noteId = :noteId AND deletedAt IS NULL ORDER BY createdAt ASC")
    fun getCommentsForNote(noteId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE noteId = :noteId AND deletedAt IS NULL ORDER BY createdAt ASC")
    suspend fun getCommentsForNoteDirect(noteId: Long): List<CommentEntity>

    @Query("SELECT * FROM comments WHERE deletedAt IS NULL ORDER BY createdAt ASC")
    fun getAllComments(): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE deletedAt IS NULL ORDER BY id ASC")
    suspend fun getAllCommentsDirect(): List<CommentEntity>

    @Query("SELECT * FROM comments WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashComments(): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    suspend fun getTrashCommentsDirect(): List<CommentEntity>

    @Query("SELECT COUNT(*) FROM comments WHERE deletedAt IS NOT NULL")
    fun getTrashCommentsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM comments WHERE noteId = :noteId AND deletedAt IS NULL")
    fun getCommentCount(noteId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM comments WHERE noteId = :noteId AND deletedAt IS NULL")
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

    @Query("UPDATE comments SET deletedAt = :deletedAt WHERE id = :id OR parentId = :id")
    suspend fun setCommentAndRepliesDeletedAt(id: Long, deletedAt: Long?)

    @Query("UPDATE comments SET deletedAt = :deletedAt WHERE noteId = :noteId")
    suspend fun setCommentsByNoteDeletedAt(noteId: Long, deletedAt: Long?)

    @Query("UPDATE comments SET deletedAt = NULL WHERE id = :id OR parentId = :id")
    suspend fun restoreCommentAndReplies(id: Long)

    @Query("UPDATE comments SET deletedAt = NULL WHERE noteId = :noteId")
    suspend fun restoreCommentsByNoteId(noteId: Long)

    @Query("UPDATE comments SET deletedAt = NULL WHERE deletedAt IS NOT NULL")
    suspend fun restoreAllComments()

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :id OR parentId = :id")
    suspend fun deleteCommentAndReplies(id: Long)

    @Query("DELETE FROM comments WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrashComments()

    @Query("DELETE FROM comments WHERE deletedAt IS NOT NULL AND deletedAt < :cutoffTimestamp")
    suspend fun deleteTrashCommentsOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()
}
