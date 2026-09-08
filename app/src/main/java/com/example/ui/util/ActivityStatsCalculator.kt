package com.example.ui.util

import com.example.data.local.CommentEntity
import com.example.data.local.NoteEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TrendDataPoint(
    val label: String,
    val noteCount: Int,
    val commentCount: Int,
    val timestamp: Long
)

data class ActivityStats(
    val totalNotes: Int,
    val totalComments: Int,
    val avgNotesPerWeek: Float,
    val peakPeriodLabel: String,
    val peakPeriodCount: Int,
    val weeklyPoints: List<TrendDataPoint>,
    val dailyPoints: List<TrendDataPoint>
)

object ActivityStatsCalculator {

    fun computeStats(
        notes: List<NoteEntity>,
        allComments: List<CommentEntity> = emptyList()
    ): ActivityStats {
        val totalNotes = notes.size
        val totalComments = allComments.size

        val weeklyPoints = computeWeeklyPoints(notes, allComments)
        val dailyPoints = computeDailyPoints(notes, allComments)

        val maxWeekly = weeklyPoints.maxByOrNull { it.noteCount }
        val peakLabel = maxWeekly?.label ?: "N/A"
        val peakCount = maxWeekly?.noteCount ?: 0

        val totalNotesInWeeks = weeklyPoints.sumOf { it.noteCount }
        val avgNotesPerWeek = if (weeklyPoints.isNotEmpty()) {
            totalNotesInWeeks.toFloat() / weeklyPoints.size.toFloat()
        } else 0f

        return ActivityStats(
            totalNotes = totalNotes,
            totalComments = totalComments,
            avgNotesPerWeek = avgNotesPerWeek,
            peakPeriodLabel = peakLabel,
            peakPeriodCount = peakCount,
            weeklyPoints = weeklyPoints,
            dailyPoints = dailyPoints
        )
    }

    private fun computeWeeklyPoints(
        notes: List<NoteEntity>,
        comments: List<CommentEntity>
    ): List<TrendDataPoint> {
        val calendar = Calendar.getInstance()
        val points = mutableListOf<TrendDataPoint>()
        val weekMs = 7 * 24 * 60 * 60 * 1000L

        // Current time aligned to week end
        val now = System.currentTimeMillis()

        for (i in 7 downTo 0) {
            val weekEnd = now - (i * weekMs)
            val weekStart = weekEnd - weekMs

            calendar.timeInMillis = weekStart
            val startWeekDay = calendar.get(Calendar.DAY_OF_MONTH)
            val startMonth = calendar.get(Calendar.MONTH) + 1

            val label = if (i == 0) "T.Này" else "T-${i}"

            val noteCount = notes.count { it.createdAt in (weekStart + 1)..weekEnd }
            val commentCount = comments.count { it.createdAt in (weekStart + 1)..weekEnd }

            points.add(
                TrendDataPoint(
                    label = label,
                    noteCount = noteCount,
                    commentCount = commentCount,
                    timestamp = weekStart
                )
            )
        }

        return points
    }

    private fun computeDailyPoints(
        notes: List<NoteEntity>,
        comments: List<CommentEntity>
    ): List<TrendDataPoint> {
        val points = mutableListOf<TrendDataPoint>()
        val dayMs = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()

        val dayNames = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        val calendar = Calendar.getInstance()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = now - (i * dayMs)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + dayMs

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 is Sunday
            val dayName = if (i == 0) "Hôm nay" else dayNames[dayOfWeek - 1]

            val noteCount = notes.count { it.createdAt in dayStart until dayEnd }
            val commentCount = comments.count { it.createdAt in dayStart until dayEnd }

            points.add(
                TrendDataPoint(
                    label = dayName,
                    noteCount = noteCount,
                    commentCount = commentCount,
                    timestamp = dayStart
                )
            )
        }

        return points
    }
}
