package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StorePackageTier(
    val id: Int,
    val title: String,
    val diamondPrice: Int,
    val maxCommentsPerMinute: Int,
    val maxDailyNotes: Int,
    val badgeLabel: String,
    val description: String
) {
    DEFAULT(
        id = 0,
        title = "Gói Mặc Định",
        diamondPrice = 0,
        maxCommentsPerMinute = 32,
        maxDailyNotes = 8_000,
        badgeLabel = "Cơ bản",
        description = "Giới hạn 32 bình luận & phản hồi/phút, Giới hạn 8.000 ghi chú/ngày"
    ),
    PACKAGE_1(
        id = 1,
        title = "Gói Nạp VIP 1 (8.000 💎)",
        diamondPrice = 8_000,
        maxCommentsPerMinute = 64,
        maxDailyNotes = 12_000,
        badgeLabel = "VIP 1 (64 cmt/p • 12k notes/ngày)",
        description = "Chi phí: 8.000 💎 • Giới hạn 64 cmt/phút, 12.000 ghi chú/ngày"
    ),
    PACKAGE_2(
        id = 2,
        title = "Gói Nạp VIP 2 (12.000 💎)",
        diamondPrice = 12_000,
        maxCommentsPerMinute = 96,
        maxDailyNotes = 16_000,
        badgeLabel = "VIP 2 (96 cmt/p • 16k notes/ngày)",
        description = "Chi phí: 12.000 💎 • Giới hạn 96 cmt/phút, 16.000 ghi chú/ngày"
    );

    companion object {
        fun fromId(id: Int): StorePackageTier {
            return entries.find { it.id == id } ?: DEFAULT
        }
    }
}

data class DiamondRewardState(
    val totalDiamonds: Int = 0,
    val targetDiamonds: Int = 8000,
    val notesCreatedToday: Int = 0,
    val maxDailyNotes: Int = 8000,
    val commentsInCurrentMinute: Int = 0,
    val maxCommentsPerMinute: Int = 32,
    val totalNotesCreatedAllTime: Int = 0,
    val totalCommentsAllTime: Int = 0,
    val isGoalCompleted: Boolean = false,
    val currentTier: StorePackageTier = StorePackageTier.DEFAULT
)

class DiamondRewardManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("diamond_rewards_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DIAMOND_GOAL = 8000
        const val ONE_MINUTE_MS = 60_000L

        private const val KEY_TOTAL_DIAMONDS = "key_total_diamonds"
        private const val KEY_TOTAL_NOTES_ALL_TIME = "key_total_notes_all_time"
        private const val KEY_TOTAL_COMMENTS_ALL_TIME = "key_total_comments_all_time"
        private const val KEY_DAY_KEY = "key_current_day"
        private const val KEY_TODAY_NOTES_COUNT = "key_today_notes_count"
        private const val KEY_ACTIVE_TIER_ID = "key_active_tier_id"
    }

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val commentTimestamps = mutableListOf<Long>()

    private val _rewardState = MutableStateFlow(loadCurrentState())
    val rewardState: StateFlow<DiamondRewardState> = _rewardState.asStateFlow()

    private fun getTodayKey(): String = dateFormat.format(Date())

    private fun loadCurrentState(): DiamondRewardState {
        val totalDiamonds = prefs.getInt(KEY_TOTAL_DIAMONDS, 0)
        val totalNotes = prefs.getInt(KEY_TOTAL_NOTES_ALL_TIME, 0)
        val totalComments = prefs.getInt(KEY_TOTAL_COMMENTS_ALL_TIME, 0)
        val tierId = prefs.getInt(KEY_ACTIVE_TIER_ID, 0)
        val tier = StorePackageTier.fromId(tierId)

        val savedDay = prefs.getString(KEY_DAY_KEY, "")
        val currentDay = getTodayKey()
        val todayNotes = if (savedDay == currentDay) {
            prefs.getInt(KEY_TODAY_NOTES_COUNT, 0)
        } else {
            0
        }

        pruneOldCommentTimestamps()

        return DiamondRewardState(
            totalDiamonds = totalDiamonds,
            targetDiamonds = DIAMOND_GOAL,
            notesCreatedToday = todayNotes,
            maxDailyNotes = tier.maxDailyNotes,
            commentsInCurrentMinute = commentTimestamps.size,
            maxCommentsPerMinute = tier.maxCommentsPerMinute,
            totalNotesCreatedAllTime = totalNotes,
            totalCommentsAllTime = totalComments,
            isGoalCompleted = totalDiamonds >= DIAMOND_GOAL,
            currentTier = tier
        )
    }

    private fun pruneOldCommentTimestamps() {
        val now = System.currentTimeMillis()
        val threshold = now - ONE_MINUTE_MS
        commentTimestamps.removeAll { it < threshold }
    }

    /**
     * Check if user can create a note today under their tier's daily notes limit.
     */
    fun canCreateNoteToday(): Boolean {
        refreshDailyCountsIfNeeded()
        return _rewardState.value.notesCreatedToday < _rewardState.value.maxDailyNotes
    }

    /**
     * Check if user can post a comment/reply under their tier's rate limit.
     */
    fun canPostCommentNow(): Boolean {
        pruneOldCommentTimestamps()
        return commentTimestamps.size < _rewardState.value.maxCommentsPerMinute
    }

    fun getRemainingSecondsForCommentRateLimit(): Int {
        pruneOldCommentTimestamps()
        if (commentTimestamps.size < _rewardState.value.maxCommentsPerMinute) return 0
        val oldestInWindow = commentTimestamps.firstOrNull() ?: return 0
        val remainingMs = (oldestInWindow + ONE_MINUTE_MS) - System.currentTimeMillis()
        return ((remainingMs / 1000) + 1).coerceAtLeast(1).toInt()
    }

    private fun refreshDailyCountsIfNeeded() {
        val currentDay = getTodayKey()
        val savedDay = prefs.getString(KEY_DAY_KEY, "")
        if (savedDay != currentDay) {
            prefs.edit()
                .putString(KEY_DAY_KEY, currentDay)
                .putInt(KEY_TODAY_NOTES_COUNT, 0)
                .apply()
            updateState()
        }
    }

    /**
     * Record a newly created note. Increments diamond count by +1 and records daily count.
     */
    @Synchronized
    fun onNoteCreated(): Boolean {
        refreshDailyCountsIfNeeded()
        val currentState = _rewardState.value
        val currentTodayCount = currentState.notesCreatedToday
        if (currentTodayCount >= currentState.maxDailyNotes) {
            return false
        }

        val newTodayCount = currentTodayCount + 1
        val newTotalDiamonds = currentState.totalDiamonds + 1
        val newTotalNotes = currentState.totalNotesCreatedAllTime + 1

        prefs.edit()
            .putString(KEY_DAY_KEY, getTodayKey())
            .putInt(KEY_TODAY_NOTES_COUNT, newTodayCount)
            .putInt(KEY_TOTAL_DIAMONDS, newTotalDiamonds)
            .putInt(KEY_TOTAL_NOTES_ALL_TIME, newTotalNotes)
            .apply()

        updateState()
        return true
    }

    /**
     * Record a newly posted comment or reply. Increments diamond count by +1 and records rate limit timestamp.
     */
    @Synchronized
    fun onCommentOrReplyCreated(): Boolean {
        pruneOldCommentTimestamps()
        val currentState = _rewardState.value
        if (commentTimestamps.size >= currentState.maxCommentsPerMinute) {
            return false
        }

        val now = System.currentTimeMillis()
        commentTimestamps.add(now)

        val newTotalDiamonds = currentState.totalDiamonds + 1
        val newTotalComments = currentState.totalCommentsAllTime + 1

        prefs.edit()
            .putInt(KEY_TOTAL_DIAMONDS, newTotalDiamonds)
            .putInt(KEY_TOTAL_COMMENTS_ALL_TIME, newTotalComments)
            .apply()

        updateState()
        return true
    }

    /**
     * Top up diamonds directly in store
     */
    @Synchronized
    fun topUpDiamonds(amount: Int) {
        if (amount <= 0) return
        val newTotalDiamonds = _rewardState.value.totalDiamonds + amount
        prefs.edit()
            .putInt(KEY_TOTAL_DIAMONDS, newTotalDiamonds)
            .apply()
        updateState()
    }

    /**
     * Purchase/activate a store package tier using existing diamonds
     */
    @Synchronized
    fun activateStorePackage(tier: StorePackageTier): Boolean {
        val currentDiamonds = _rewardState.value.totalDiamonds
        if (currentDiamonds < tier.diamondPrice) {
            return false
        }

        val newTotalDiamonds = currentDiamonds - tier.diamondPrice
        prefs.edit()
            .putInt(KEY_TOTAL_DIAMONDS, newTotalDiamonds)
            .putInt(KEY_ACTIVE_TIER_ID, tier.id)
            .apply()

        updateState()
        return true
    }

    /**
     * Quick top up diamonds and immediately activate package
     */
    @Synchronized
    fun topUpAndActivatePackage(tier: StorePackageTier) {
        prefs.edit()
            .putInt(KEY_ACTIVE_TIER_ID, tier.id)
            .putInt(KEY_TOTAL_DIAMONDS, _rewardState.value.totalDiamonds + tier.diamondPrice)
            .apply()
        updateState()
    }

    fun refreshRateLimitTracker() {
        pruneOldCommentTimestamps()
        updateState()
    }

    private fun updateState() {
        _rewardState.value = loadCurrentState()
    }
}

