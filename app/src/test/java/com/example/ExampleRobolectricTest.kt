package com.example

import com.example.data.local.LessonProgressDao
import com.example.data.local.LessonProgressEntity
import com.example.model.SessionMode
import com.example.domain.SpacedRepetitionScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class InMemoryLessonProgressDao : LessonProgressDao {
    private val store = MutableStateFlow<Map<String, LessonProgressEntity>>(emptyMap())

    override fun observeAllProgress(): Flow<List<LessonProgressEntity>> =
        store.map { it.values.toList() }

    override suspend fun getProgressById(lessonId: String): LessonProgressEntity? =
        store.value[lessonId]

    override suspend fun upsertProgress(entity: LessonProgressEntity) {
        store.value = store.value + (entity.lessonId to entity)
    }

    override suspend fun clearAll() {
        store.value = emptyMap()
    }

    override fun observePendingSyncCount(): Flow<Int> =
        store.map { map -> map.values.count { it.pendingRemoteSync } }
}

class ExampleRobolectricTest {

    @Test
    fun `progress dao stores per-lesson practice and review turns independently`() = runTest {
        val dao = InMemoryLessonProgressDao()

        dao.upsertProgress(
            LessonProgressEntity(
                lessonId = "cafe_order",
                practiceTurnIndex = 2,
                practiceCompletedCount = 1,
                reviewTurnIndex = 1,
                reviewCompletedCount = 0,
                nextReviewEpochDay = 20_501L
            )
        )
        dao.upsertProgress(
            LessonProgressEntity(
                lessonId = "ask_directions",
                practiceTurnIndex = 1,
                practiceCompletedCount = 0,
                reviewTurnIndex = 0,
                reviewCompletedCount = 0,
                nextReviewEpochDay = null
            )
        )

        val cafe = dao.getProgressById("cafe_order")
        val directions = dao.getProgressById("ask_directions")
        assertNotNull(cafe)
        assertNotNull(directions)
        assertEquals(2, cafe?.practiceTurnIndex)
        assertEquals(1, cafe?.reviewTurnIndex)
        assertEquals(1, directions?.practiceTurnIndex)
        assertEquals(2, dao.observePendingSyncCount().first())
    }

    @Test
    fun `review due check identifies today and overdue dates`() {
        val today = 20_500L
        assertTrue(SpacedRepetitionScheduler.isReviewDue(20_500L, today))
        assertTrue(SpacedRepetitionScheduler.isReviewDue(20_499L, today))
        assertEquals("Hoy", SpacedRepetitionScheduler.formatSuggestedReviewLabelEs(20_500L, today))
        assertEquals("Repaso pendiente", SpacedRepetitionScheduler.formatSuggestedReviewLabelEs(20_498L, today))
        assertEquals(SessionMode.PRACTICE, SessionMode.valueOf("PRACTICE"))
    }
}
