package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StreakState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val hasInteractedToday: Boolean = false,
    val lastInteractionDateStr: String = "",
    val activeDaysPastWeek: List<Boolean> = List(7) { false },
    val streakLevelName: String = "Người mới",
    val nextMilestone: Int = 3
)

class StreakManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("daily_streak_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    private val _streakState = MutableStateFlow(calculateCurrentState())
    val streakState: StateFlow<StreakState> = _streakState.asStateFlow()

    init {
        // Refresh state on creation to handle day roll-over
        refreshState()
    }

    private fun getTodayDateStr(): String = dateFormat.format(Date())

    private fun getYesterdayDateStr(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    @Synchronized
    fun refreshState() {
        _streakState.value = calculateCurrentState()
    }

    private fun calculateCurrentState(): StreakState {
        val todayStr = getTodayDateStr()
        val yesterdayStr = getYesterdayDateStr()

        val lastDateStr = prefs.getString(KEY_LAST_INTERACTION_DATE, "") ?: ""
        var currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        var longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)

        val hasInteractedToday = (lastDateStr == todayStr)

        // Check if streak was broken (last interaction was before yesterday)
        if (lastDateStr.isNotEmpty() && lastDateStr != todayStr && lastDateStr != yesterdayStr) {
            currentStreak = 0
            prefs.edit().putInt(KEY_CURRENT_STREAK, 0).apply()
        }

        // Calculate past 7 days activity
        val pastWeekBooleans = (0..6).map { daysAgo ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dateKey = dateFormat.format(cal.time)
            prefs.getBoolean("activity_$dateKey", false)
        }.reversed()

        val levelName = when {
            currentStreak >= 30 -> "Huyền Thoại Ghi Chú"
            currentStreak >= 14 -> "Bậc Thầy Kiên Trì"
            currentStreak >= 7 -> "Chuyên Gia Thói Quản"
            currentStreak >= 3 -> "Ngọn Lửa Bùng Cháy"
            currentStreak >= 1 -> "Khởi Đầu Tốt Đẹp"
            else -> "Chưa Khởi Động"
        }

        val nextMilestone = when {
            currentStreak < 3 -> 3
            currentStreak < 7 -> 7
            currentStreak < 14 -> 14
            currentStreak < 30 -> 30
            else -> currentStreak + 10
        }

        return StreakState(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            hasInteractedToday = hasInteractedToday,
            lastInteractionDateStr = lastDateStr,
            activeDaysPastWeek = pastWeekBooleans,
            streakLevelName = levelName,
            nextMilestone = nextMilestone
        )
    }

    /**
     * Call whenever user interacts with notes (creates, edits, views, comments, searches, opens app)
     */
    @Synchronized
    fun recordInteraction(): Boolean {
        val todayStr = getTodayDateStr()
        val yesterdayStr = getYesterdayDateStr()
        val lastDateStr = prefs.getString(KEY_LAST_INTERACTION_DATE, "") ?: ""

        var currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        var longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)

        // Record activity boolean for today
        prefs.edit().putBoolean("activity_$todayStr", true).apply()

        var isNewDayInteraction = false

        if (lastDateStr == todayStr) {
            // Already recorded today, maintain streak
            isNewDayInteraction = false
        } else if (lastDateStr == yesterdayStr) {
            // Consecutive day!
            currentStreak += 1
            isNewDayInteraction = true
        } else {
            // Streak broken or first interaction
            currentStreak = 1
            isNewDayInteraction = true
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        prefs.edit()
            .putString(KEY_LAST_INTERACTION_DATE, todayStr)
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .apply()

        refreshState()
        return isNewDayInteraction
    }

    companion object {
        private const val KEY_LAST_INTERACTION_DATE = "key_last_interaction_date"
        private const val KEY_CURRENT_STREAK = "key_current_streak"
        private const val KEY_LONGEST_STREAK = "key_longest_streak"
    }
}
