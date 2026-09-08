package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.JsonBackupHelper
import com.example.data.local.AppDatabase
import com.example.data.local.CommentEntity
import com.example.data.local.NoteEntity
import com.example.data.repository.AutoBackupManager
import com.example.data.repository.AutoBackupState
import com.example.data.repository.BackupFileInfo
import com.example.data.repository.DiamondRewardManager
import com.example.data.repository.DiamondRewardState
import com.example.data.repository.ImportSummary
import com.example.data.repository.NoteRepository
import com.example.data.repository.StorePackageTier
import com.example.data.repository.StreakManager
import com.example.data.repository.StreakState
import com.example.data.repository.ThemeManager
import com.example.data.repository.ThemeMode
import com.example.ui.util.ActivityStats
import com.example.ui.util.ActivityStatsCalculator
import com.example.ui.util.SearchUtils
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CommentWithReplies(
    val comment: CommentEntity,
    val replies: List<CommentEntity>
)

data class CommentsUiState(
    val totalCount: Int = 0,
    val displayedCount: Int = 0,
    val hasEarlierComments: Boolean = false,
    val remainingEarlierCount: Int = 0,
    val commentThreads: List<CommentWithReplies> = emptyList(),
    val pageSize: Int = 200
)

enum class NoteSortOrder(val displayName: String) {
    NEWEST_FIRST("Mới nhất trước (Newest first)"),
    OLDEST_FIRST("Cũ nhất trước (Oldest first)")
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    private val rewardManager: DiamondRewardManager = DiamondRewardManager(application)
    private val autoBackupManager: AutoBackupManager = AutoBackupManager(application)
    private val streakManager: StreakManager = StreakManager(application)
    private val themeManager: ThemeManager = ThemeManager(application)

    val rewardState: StateFlow<DiamondRewardState> = rewardManager.rewardState
    val autoBackupState: StateFlow<AutoBackupState> = autoBackupManager.backupState
    val streakState: StateFlow<StreakState> = streakManager.streakState
    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode

    fun setThemeMode(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }

    fun toggleTheme() {
        themeManager.toggleLightDark()
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = NoteRepository(db.noteDao(), db.commentDao())
        viewModelScope.launch {
            autoBackupManager.checkAndRunDailyBackup(repository)
        }
        recordInteraction()
    }

    fun recordInteraction() {
        streakManager.recordInteraction()
    }

    // Status message for Snackbars/Dialogs
    private val _importExportMessage = MutableStateFlow<String?>(null)
    val importExportMessage: StateFlow<String?> = _importExportMessage.asStateFlow()

    fun setImportExportMessage(message: String?) {
        _importExportMessage.value = message
    }

    fun clearImportExportMessage() {
        _importExportMessage.value = null
    }

    // Warning message for Rate Limits & Diamond celebrations
    private val _rateLimitWarning = MutableStateFlow<String?>(null)
    val rateLimitWarning: StateFlow<String?> = _rateLimitWarning.asStateFlow()

    fun clearRateLimitWarning() {
        _rateLimitWarning.value = null
    }

    fun setRateLimitWarning(msg: String?) {
        _rateLimitWarning.value = msg
    }

    // Sort order for notes by creation date
    val sortOrder = MutableStateFlow(NoteSortOrder.NEWEST_FIRST)

    fun setSortOrder(order: NoteSortOrder) {
        sortOrder.value = order
    }

    fun toggleSortOrder() {
        sortOrder.value = if (sortOrder.value == NoteSortOrder.NEWEST_FIRST) {
            NoteSortOrder.OLDEST_FIRST
        } else {
            NoteSortOrder.NEWEST_FIRST
        }
    }

    // Search query for notes
    val searchQuery = MutableStateFlow("")

    // Selected category/folder filter (null means All)
    val selectedCategory = MutableStateFlow<String?>(null)

    // Selected tag/label filter (null means All)
    val selectedTag = MutableStateFlow<String?>(null)

    // All notes from repository
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All comments from repository
    val allComments: StateFlow<List<CommentEntity>> = repository.allComments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated note and comment activity trend statistics
    val activityStats: StateFlow<ActivityStats> = combine(allNotes, allComments) { notes, comments ->
        ActivityStatsCalculator.computeStats(notes, comments)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityStatsCalculator.computeStats(emptyList()))

    // All active categories across notes
    val activeCategories: StateFlow<List<String>> = allNotes.combine(flowOf(Unit)) { notes, _ ->
        notes.map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active tags across notes
    val activeTags: StateFlow<List<String>> = allNotes.combine(flowOf(Unit)) { notes, _ ->
        notes.flatMap { it.tagList }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered notes (separated into pinned and unpinned, sorted according to sortOrder)
    val filteredNotes = combine(allNotes, searchQuery, selectedCategory, selectedTag, sortOrder) { notes, query, category, tag, sort ->
        val filtered = notes.filter { note ->
            // Category filter
            val matchesCategory = category.isNullOrBlank() || note.category.equals(category, ignoreCase = true)

            // Tag filter
            val matchesTag = tag.isNullOrBlank() || note.tagList.any { it.equals(tag, ignoreCase = true) }

            // Search query filter
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                SearchUtils.matches(note.title, query) ||
                SearchUtils.matches(note.description, query) ||
                SearchUtils.matches(note.category, query) ||
                note.tagList.any { SearchUtils.matches(it, query) }
            }

            matchesCategory && matchesTag && matchesQuery
        }

        val sorted = when (sort) {
            NoteSortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.createdAt }
            NoteSortOrder.OLDEST_FIRST -> filtered.sortedBy { it.createdAt }
        }

        val pinned = sorted.filter { it.isPinned }
        val unpinned = sorted.filter { !it.isPinned }
        Pair(pinned, unpinned)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(emptyList(), emptyList()))

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun clearSearchQuery() {
        searchQuery.value = ""
    }

    fun selectCategory(category: String?) {
        selectedCategory.value = if (selectedCategory.value == category) null else category
    }

    fun selectTag(tag: String?) {
        selectedTag.value = if (selectedTag.value == tag) null else tag
    }

    fun clearCategoryFilter() {
        selectedCategory.value = null
    }

    fun clearTagFilter() {
        selectedTag.value = null
    }

    fun clearAllFilters() {
        selectedCategory.value = null
        selectedTag.value = null
        searchQuery.value = ""
    }

    // Selected Note for detail view
    private val _selectedNoteId = MutableStateFlow<Long?>(null)
    val selectedNoteId: StateFlow<Long?> = _selectedNoteId.asStateFlow()

    val selectedNote: StateFlow<NoteEntity?> = _selectedNoteId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getNote(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current page display limit for comments & replies (default 200)
    val pageSize = 200
    private val _visibleCommentsLimit = MutableStateFlow(200)
    val visibleCommentsLimit: StateFlow<Int> = _visibleCommentsLimit.asStateFlow()

    // Comments & Replies UI state with 200 items per page pagination
    val commentsUiState: StateFlow<CommentsUiState> = combine(
        _selectedNoteId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getComments(id)
        },
        _visibleCommentsLimit
    ) { allItems, limit ->
        val totalCount = allItems.size
        // Determine window of items to display:
        // By default, showing up to 'limit' items (e.g. the most recent 200 items).
        // If totalCount > limit, earlier comments can be loaded via "Tải bình luận trước"
        val hasEarlier = totalCount > limit
        val displayedItems = if (hasEarlier) {
            allItems.takeLast(limit)
        } else {
            allItems
        }
        val remainingEarlier = if (hasEarlier) totalCount - limit else 0

        // Separate into root comments and replies
        val roots = displayedItems.filter { it.parentId == null }
        val replies = displayedItems.filter { it.parentId != null }

        // Group replies by parentId
        val repliesByParent = replies.groupBy { it.parentId }

        // Build threads:
        // Pinned comments appear at the top
        // Within each thread, pinned replies appear at the top
        val threads = roots
            .sortedWith(compareByDescending<CommentEntity> { it.isPinned }.thenBy { it.createdAt })
            .map { root ->
                val threadReplies = (repliesByParent[root.id] ?: emptyList())
                    .sortedWith(compareByDescending<CommentEntity> { it.isPinned }.thenBy { it.createdAt })
                CommentWithReplies(comment = root, replies = threadReplies)
            }

        CommentsUiState(
            totalCount = totalCount,
            displayedCount = displayedItems.size,
            hasEarlierComments = hasEarlier,
            remainingEarlierCount = remainingEarlier,
            commentThreads = threads,
            pageSize = pageSize
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CommentsUiState())

    // Active reply target (comment or reply)
    private val _replyingTo = MutableStateFlow<CommentEntity?>(null)
    val replyingTo: StateFlow<CommentEntity?> = _replyingTo.asStateFlow()

    // Note creation / edit dialog state
    private val _editingNote = MutableStateFlow<NoteEntity?>(null)
    val editingNote: StateFlow<NoteEntity?> = _editingNote.asStateFlow()

    private val _isCreatingNewNote = MutableStateFlow(false)
    val isCreatingNewNote: StateFlow<Boolean> = _isCreatingNewNote.asStateFlow()

    fun selectNote(noteId: Long?) {
        _selectedNoteId.value = noteId
        _visibleCommentsLimit.value = pageSize
        _replyingTo.value = null
    }

    fun loadEarlierComments() {
        _visibleCommentsLimit.value += pageSize
    }

    fun startCreateNote() {
        _isCreatingNewNote.value = true
        _editingNote.value = null
    }

    fun startEditNote(note: NoteEntity) {
        _editingNote.value = note
        _isCreatingNewNote.value = false
    }

    fun dismissNoteDialog() {
        _isCreatingNewNote.value = false
        _editingNote.value = null
    }

    fun saveNote(
        title: String,
        description: String,
        isPinned: Boolean,
        category: String = "",
        tags: String = ""
    ) {
        viewModelScope.launch {
            val currentEditing = _editingNote.value
            if (currentEditing != null) {
                repository.updateNote(
                    currentEditing.copy(
                        title = title.trim(),
                        description = description.trim(),
                        isPinned = isPinned,
                        category = category.trim(),
                        tags = tags.trim()
                    )
                )
                dismissNoteDialog()
            } else {
                // Check daily limit for notes
                val maxNotes = rewardState.value.maxDailyNotes
                if (!rewardManager.canCreateNoteToday()) {
                    _rateLimitWarning.value = "Đã đạt giới hạn $maxNotes ghi chú/ngày. Hãy nâng cấp gói nạp tại Cửa hàng hoặc quay lại vào ngày mai!"
                    return@launch
                }

                val newId = repository.insertNote(
                    title = title.trim(),
                    description = description.trim(),
                    isPinned = isPinned,
                    category = category.trim(),
                    tags = tags.trim()
                )

                // Accumulate 1 diamond for new note
                rewardManager.onNoteCreated()
                dismissNoteDialog()
            }
            recordInteraction()
        }
    }

    fun toggleNotePinned(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleNotePinned(note.id, note.isPinned)
            recordInteraction()
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            if (_selectedNoteId.value == noteId) {
                _selectedNoteId.value = null
            }
            recordInteraction()
        }
    }

    fun setReplyingTo(target: CommentEntity?) {
        _replyingTo.value = target
    }

    fun addCommentOrReply(content: String, authorName: String) {
        val noteId = _selectedNoteId.value ?: return
        if (content.isBlank()) return

        // Check rate limit for comments & replies
        val maxComments = rewardState.value.maxCommentsPerMinute
        if (!rewardManager.canPostCommentNow()) {
            val waitSec = rewardManager.getRemainingSecondsForCommentRateLimit()
            _rateLimitWarning.value = "Đã đạt giới hạn $maxComments bình luận và phản hồi/phút. Vui lòng chờ $waitSec giây hoặc nâng cấp gói nạp tại Cửa hàng!"
            return
        }

        val replying = _replyingTo.value
        viewModelScope.launch {
            if (replying != null) {
                // If replying to a reply, find its root parent or attach to parent
                val parentId = replying.parentId ?: replying.id
                repository.insertComment(
                    noteId = noteId,
                    content = content.trim(),
                    authorName = authorName.ifBlank { "Bạn" },
                    parentId = parentId,
                    replyToAuthor = replying.authorName
                )
            } else {
                repository.insertComment(
                    noteId = noteId,
                    content = content.trim(),
                    authorName = authorName.ifBlank { "Bạn" },
                    parentId = null,
                    replyToAuthor = null
                )
            }
            // Accumulate 1 diamond for comment/reply
            rewardManager.onCommentOrReplyCreated()
            _replyingTo.value = null
            recordInteraction()
        }
    }

    fun toggleCommentPinned(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleCommentPinned(comment.id, comment.isPinned)
            recordInteraction()
        }
    }

    fun updateComment(commentId: Long, newContent: String, newAuthorName: String) {
        viewModelScope.launch {
            repository.updateComment(commentId, newContent, newAuthorName)
            recordInteraction()
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            repository.deleteComment(commentId)
            if (_replyingTo.value?.id == commentId) {
                _replyingTo.value = null
            }
            recordInteraction()
        }
    }

    // Store operations
    fun topUpDiamonds(amount: Int) {
        rewardManager.topUpDiamonds(amount)
        _importExportMessage.value = "Đã nạp thành công $amount 💎 vào tài khoản!"
    }

    fun activateStorePackage(tier: StorePackageTier): Boolean {
        val success = rewardManager.activateStorePackage(tier)
        if (success) {
            _importExportMessage.value = "Kích hoạt thành công ${tier.title}! Giới hạn mới: ${tier.maxCommentsPerMinute} cmt/phút, ${tier.maxDailyNotes} ghi chú/ngày."
        } else {
            _rateLimitWarning.value = "Bạn cần ${tier.diamondPrice} 💎 để kích hoạt ${tier.title}. Vui lòng nạp thêm kim cương hoặc tích lũy thêm!"
        }
        return success
    }

    fun topUpAndActivatePackage(tier: StorePackageTier) {
        rewardManager.topUpAndActivatePackage(tier)
        _importExportMessage.value = "Nạp & kích hoạt thành công ${tier.title}! Giới hạn mới: ${tier.maxCommentsPerMinute} cmt/phút, ${tier.maxDailyNotes} ghi chú/ngày."
    }

    suspend fun exportAllNotesJson(): String {
        val notes = repository.getAllNotesDirect()
        val comments = repository.getAllCommentsDirect()
        return JsonBackupHelper.exportAllDataToJson(notes, comments)
    }

    suspend fun exportSingleNoteJson(noteId: Long): String? {
        val note = repository.getNoteByIdDirect(noteId) ?: return null
        val comments = repository.getCommentsForNoteDirect(noteId)
        return JsonBackupHelper.exportSingleNoteToJson(note, comments)
    }

    suspend fun importJsonData(jsonString: String, replaceExisting: Boolean): Result<ImportSummary> {
        val parseResult = JsonBackupHelper.parseJson(jsonString)
        if (parseResult.isFailure) {
            return Result.failure(parseResult.exceptionOrNull() ?: Exception("Lỗi phân tích cú pháp JSON"))
        }
        val backupData = parseResult.getOrThrow()
        return try {
            val summary = repository.importBackupData(backupData, replaceExisting)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Auto Backup & Local Backups operations
    fun setAutoBackupEnabled(enabled: Boolean) {
        autoBackupManager.setAutoBackupEnabled(enabled)
        if (enabled) {
            viewModelScope.launch {
                autoBackupManager.checkAndRunDailyBackup(repository)
            }
        }
    }

    fun triggerManualBackup(onFinished: (Result<File>) -> Unit = {}) {
        viewModelScope.launch {
            val result = autoBackupManager.performBackup(repository, isAutomatic = false)
            if (result.isSuccess) {
                _importExportMessage.value = "Đã sao lưu thành công tệp: ${result.getOrNull()?.name}"
            } else {
                _importExportMessage.value = "Lỗi khi sao lưu: ${result.exceptionOrNull()?.localizedMessage}"
            }
            onFinished(result)
        }
    }

    fun getBackupFiles(): List<BackupFileInfo> {
        return autoBackupManager.getBackupFiles()
    }

    fun deleteBackupFile(file: File): Boolean {
        val deleted = autoBackupManager.deleteBackupFile(file)
        if (deleted) {
            _importExportMessage.value = "Đã xóa bản sao lưu ${file.name}"
        }
        return deleted
    }

    suspend fun restoreFromLocalBackup(file: File, replaceExisting: Boolean): Result<ImportSummary> {
        val result = autoBackupManager.restoreFromBackupFile(file, repository, replaceExisting)
        if (result.isSuccess) {
            val summary = result.getOrThrow()
            _importExportMessage.value = "Khôi phục thành công: ${summary.notesImported} ghi chú, ${summary.commentsImported} bình luận, ${summary.repliesImported} phản hồi!"
        }
        return result
    }
}
