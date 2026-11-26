package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Spaced repetition scheduler for HablaYa lessons.
 * - First suggested review after finishing practice: next day (+1 day).
 * - Each subsequent completed review extends the interval: +3, +7, +14, +30 days.
 */
object SpacedRepetitionScheduler {

    private val reviewIntervalsDays = listOf(1L, 3L, 7L, 14L, 30L)

    fun currentEpochDay(nowMillis: Long = System.currentTimeMillis()): Long {
        return TimeUnit.MILLISECONDS.toDays(nowMillis)
    }

    /**
     * Computes the next suggested review epoch day.
     * @param completedReviewsCount Number of reviews completed BEFORE this completion (0 when finishing initial practice).
     */
    fun computeNextReviewEpochDay(
        completedReviewsCount: Int,
        todayEpochDay: Long = currentEpochDay()
    ): Long {
        val index = completedReviewsCount.coerceIn(0, reviewIntervalsDays.lastIndex)
        val deltaDays = reviewIntervalsDays[index]
        return todayEpochDay + deltaDays
    }

    fun formatSuggestedReviewLabelEs(
        nextReviewEpochDay: Long,
        todayEpochDay: Long = currentEpochDay()
    ): String {
        val diff = nextReviewEpochDay - todayEpochDay
        return when {
            diff < 0 -> "Repaso pendiente"
            diff == 0L -> "Hoy"
            diff == 1L -> "Mañana"
            diff in 2L..6L -> "En $diff días"
            diff == 7L -> "En 1 semana"
            else -> {
                val cal = Calendar.getInstance()
                cal.timeInMillis = TimeUnit.DAYS.toMillis(nextReviewEpochDay)
                val formatter = SimpleDateFormat("d 'de' MMM", Locale("es", "ES"))
                formatter.format(cal.time)
            }
        }
    }

    fun isReviewDue(
        nextReviewEpochDay: Long?,
        todayEpochDay: Long = currentEpochDay()
    ): Boolean {
        if (nextReviewEpochDay == null) return false
        return nextReviewEpochDay <= todayEpochDay
    }
}
