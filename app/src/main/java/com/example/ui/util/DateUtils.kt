package com.example.ui.util

import java.util.Calendar

object DateUtils {

    /**
     * Formats elapsed time in the exact format:
     * "[X] năm [X] tháng [X] ngày [X] giờ [X] phút [X] giây trước"
     * Ví dụ: 1 năm 1 tháng 0 ngày 0 giờ 0 phút 0 giây trước
     */
    fun formatDetailedElapsedTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        if (timestamp >= now) {
            return "0 năm 0 tháng 0 ngày 0 giờ 0 phút 0 giây trước"
        }

        val thenCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        var years = nowCal.get(Calendar.YEAR) - thenCal.get(Calendar.YEAR)
        var months = nowCal.get(Calendar.MONTH) - thenCal.get(Calendar.MONTH)
        var days = nowCal.get(Calendar.DAY_OF_MONTH) - thenCal.get(Calendar.DAY_OF_MONTH)
        var hours = nowCal.get(Calendar.HOUR_OF_DAY) - thenCal.get(Calendar.HOUR_OF_DAY)
        var minutes = nowCal.get(Calendar.MINUTE) - thenCal.get(Calendar.MINUTE)
        var seconds = nowCal.get(Calendar.SECOND) - thenCal.get(Calendar.SECOND)

        if (seconds < 0) {
            seconds += 60
            minutes--
        }
        if (minutes < 0) {
            minutes += 60
            hours--
        }
        if (hours < 0) {
            hours += 24
            days--
        }
        if (days < 0) {
            val prevMonthCal = (nowCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            days += prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            months--
        }
        if (months < 0) {
            months += 12
            years--
        }
        if (years < 0) {
            years = 0
            months = 0
            days = 0
            hours = 0
            minutes = 0
            seconds = 0
        }

        return "$years năm $months tháng $days ngày $hours giờ $minutes phút $seconds giây trước"
    }

    fun formatRelativeTime(timestamp: Long): String {
        return formatDetailedElapsedTime(timestamp)
    }
}
