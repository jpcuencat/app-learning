package com.example.data.repository

import com.example.data.LessonCatalog
import com.example.data.local.LessonProgressDao
import com.example.data.local.LessonProgressEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesStore
import com.example.domain.SpacedRepetitionScheduler
import com.example.model.Lesson
import com.example.model.LessonProgressSummary
import com.example.model.PocketPhraseCard
import com.example.model.SessionMode
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface RemoteSyncPort {
    val isRemoteConnected: Boolean
    suspend fun queueProgressSnapshot(progress: LessonProgressSummary)
}

class LocalFirstSyncPort : RemoteSyncPort {
    override val isRemoteConnected: Boolean = false
    override suspend fun queueProgressSnapshot(progress: LessonProgressSummary) = Unit
}

data class CompletionResult(
    val lesson: Lesson,
    val mode: SessionMode,
    val skillPracticedEs: String,
    val nextReviewEpochDay: Long,
    val nextReviewLabelEs: String,
    val totalCompletions: Int,
    val starsEarned: Int,
    val xpEarned: Int,
    val maxCombo: Int,
    val unlockedPhraseCard: PocketPhraseCard?
)

class HablaYaRepository(
    private val progressDao: LessonProgressDao,
    private val preferencesStore: UserPreferencesStore,
    private val remoteSyncPort: RemoteSyncPort = LocalFirstSyncPort()
) {
    val lessons: List<Lesson> = LessonCatalog.lessons

    val preferencesFlow: Flow<UserPreferences> = preferencesStore.preferencesFlow

    val progressMapFlow: Flow<Map<String, LessonProgressSummary>> =
        progressDao.observeAllProgress().map { entities ->
            entities.associate { it.lessonId to it.toDomain() }
        }

    val pendingSyncCountFlow: Flow<Int> = progressDao.observePendingSyncCount()

    suspend fun saveTurnProgress(
        lessonId: String,
        mode: SessionMode,
        nextTurnIndex: Int
    ) {
        val current = progressDao.getProgressById(lessonId) ?: LessonProgressEntity(lessonId = lessonId)
        val updated = when (mode) {
            SessionMode.PRACTICE -> current.copy(
                practiceTurnIndex = nextTurnIndex,
                lastUpdatedMillis = System.currentTimeMillis(),
                pendingRemoteSync = true
            )
            SessionMode.REVIEW -> current.copy(
                reviewTurnIndex = nextTurnIndex,
                lastUpdatedMillis = System.currentTimeMillis(),
                pendingRemoteSync = true
            )
        }
        progressDao.upsertProgress(updated)
        remoteSyncPort.queueProgressSnapshot(updated.toDomain())
    }

    suspend fun completeSession(
        lessonId: String,
        mode: SessionMode,
        xpEarned: Int,
        starsEarned: Int,
        maxCombo: Int,
        todayEpochDay: Long = SpacedRepetitionScheduler.currentEpochDay()
    ): CompletionResult {
        val lesson = LessonCatalog.findLesson(lessonId)
            ?: error("Lesson not found: $lessonId")
        val current = progressDao.getProgressById(lessonId) ?: LessonProgressEntity(lessonId = lessonId)

        val clampedStars = starsEarned.coerceIn(1, 3)
        val updated = when (mode) {
            SessionMode.PRACTICE -> {
                val nextReview = SpacedRepetitionScheduler.computeNextReviewEpochDay(
                    completedReviewsCount = current.reviewCompletedCount,
                    todayEpochDay = todayEpochDay
                )
                current.copy(
                    practiceTurnIndex = 0,
                    practiceCompletedCount = current.practiceCompletedCount + 1,
                    starsEarned = maxOf(current.starsEarned, clampedStars),
                    bestXp = maxOf(current.bestXp, xpEarned),
                    nextReviewEpochDay = nextReview,
                    lastUpdatedMillis = System.currentTimeMillis(),
                    pendingRemoteSync = true
                )
            }
            SessionMode.REVIEW -> {
                val newReviewCount = current.reviewCompletedCount + 1
                val nextReview = SpacedRepetitionScheduler.computeNextReviewEpochDay(
                    completedReviewsCount = newReviewCount,
                    todayEpochDay = todayEpochDay
                )
                current.copy(
                    reviewTurnIndex = 0,
                    reviewCompletedCount = newReviewCount,
                    starsEarned = maxOf(current.starsEarned, clampedStars),
                    bestXp = maxOf(current.bestXp, xpEarned),
                    nextReviewEpochDay = nextReview,
                    lastUpdatedMillis = System.currentTimeMillis(),
                    pendingRemoteSync = true
                )
            }
        }

        progressDao.upsertProgress(updated)
        remoteSyncPort.queueProgressSnapshot(updated.toDomain())

        val card = LessonCatalog.pocketPhraseCards.firstOrNull { it.lessonId == lessonId }
        preferencesStore.recordGameRewards(
            xpEarned = xpEarned,
            todayEpochDay = todayEpochDay,
            unlockedCardId = card?.id
        )

        val variant = if (mode == SessionMode.PRACTICE) lesson.practice else lesson.review
        val nextDay = updated.nextReviewEpochDay ?: (todayEpochDay + 1L)
        return CompletionResult(
            lesson = lesson,
            mode = mode,
            skillPracticedEs = variant.skillSummaryEs,
            nextReviewEpochDay = nextDay,
            nextReviewLabelEs = SpacedRepetitionScheduler.formatSuggestedReviewLabelEs(
                nextReviewEpochDay = nextDay,
                todayEpochDay = todayEpochDay
            ),
            totalCompletions = updated.practiceCompletedCount + updated.reviewCompletedCount,
            starsEarned = clampedStars,
            xpEarned = xpEarned,
            maxCombo = maxCombo,
            unlockedPhraseCard = card
        )
    }

    suspend fun recordBlitzCompletion(
        score: Int,
        xpEarned: Int,
        todayEpochDay: Long = SpacedRepetitionScheduler.currentEpochDay()
    ) {
        preferencesStore.recordGameRewards(
            xpEarned = xpEarned,
            todayEpochDay = todayEpochDay,
            blitzScore = score
        )
    }

    suspend fun clearAllProgress() {
        progressDao.clearAll()
        preferencesStore.resetGameStats()
    }

    suspend fun setThemeMode(mode: ThemeMode) = preferencesStore.setThemeMode(mode)

    suspend fun setSelectedVoice(voiceId: String, fromComparison: Boolean = false) =
        preferencesStore.setSelectedVoice(voiceId, fromComparison)

    suspend fun setBiometricLockEnabled(enabled: Boolean) =
        preferencesStore.setBiometricLockEnabled(enabled)

    suspend fun setRemindersEnabled(enabled: Boolean) =
        preferencesStore.setRemindersEnabled(enabled)

    suspend fun setReminderHour(hour: Int) =
        preferencesStore.setReminderHour(hour)
}
