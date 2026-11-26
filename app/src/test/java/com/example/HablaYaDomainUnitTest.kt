package com.example

import com.example.data.LessonCatalog
import com.example.domain.LocalTurnEvaluator
import com.example.domain.SpacedRepetitionScheduler
import com.example.model.CefrLevel
import com.example.model.EvaluationOutcome
import com.example.ui.viewmodel.HablaYaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HablaYaDomainUnitTest {

    @Test
    fun `cafe turn 1 passes polite coffee request with helpful explanation`() {
        val cafe = LessonCatalog.findLesson("cafe_order")!!
        val turn1 = cafe.practice.turns[0]

        val feedback = LocalTurnEvaluator.evaluate(turn1, "Could I have a coffee, please?")

        assertEquals(EvaluationOutcome.PASSED, feedback.outcome)
        assertTrue(feedback.explanationEs.contains("Could I have", ignoreCase = true))
        assertTrue(feedback.reusableTipEs.isNotBlank())
    }

    @Test
    fun `cafe turn 1 asks for review when politeness is missing`() {
        val cafe = LessonCatalog.findLesson("cafe_order")!!
        val turn1 = cafe.practice.turns[0]

        val feedback = LocalTurnEvaluator.evaluate(turn1, "coffee")

        assertEquals(EvaluationOutcome.REVIEW_ANSWER, feedback.outcome)
    }

    @Test
    fun `spanish answer triggers retry asking for english`() {
        val cafe = LessonCatalog.findLesson("cafe_order")!!
        val turn1 = cafe.practice.turns[0]

        val feedback = LocalTurnEvaluator.evaluate(turn1, "Quiero un café por favor")

        assertEquals(EvaluationOutcome.RETRY, feedback.outcome)
        assertTrue(feedback.explanationEs.contains("español", ignoreCase = true))
    }

    @Test
    fun `cafe review variant evaluates tea and for here correctly`() {
        val cafe = LessonCatalog.findLesson("cafe_order")!!
        val r1 = LocalTurnEvaluator.evaluate(cafe.review.turns[0], "Could I have a green tea, please?")
        val r2 = LocalTurnEvaluator.evaluate(cafe.review.turns[1], "For here, please.")
        val r3 = LocalTurnEvaluator.evaluate(cafe.review.turns[2], "What is the total?")

        assertEquals(EvaluationOutcome.PASSED, r1.outcome)
        assertEquals(EvaluationOutcome.PASSED, r2.outcome)
        assertEquals(EvaluationOutcome.PASSED, r3.outcome)
    }

    @Test
    fun `directions lesson evaluates pharmacy repeat and left turn confirmation`() {
        val directions = LessonCatalog.findLesson("ask_directions")!!
        val p1 = LocalTurnEvaluator.evaluate(directions.practice.turns[0], "Excuse me, where is the pharmacy?")
        val p2 = LocalTurnEvaluator.evaluate(directions.practice.turns[1], "Sorry, could you repeat that, please?")
        val p3 = LocalTurnEvaluator.evaluate(directions.practice.turns[2], "So I turn left at the corner, right?")

        assertEquals(EvaluationOutcome.PASSED, p1.outcome)
        assertEquals(EvaluationOutcome.PASSED, p2.outcome)
        assertEquals(EvaluationOutcome.PASSED, p3.outcome)
    }

    @Test
    fun `unexpected multi-word english response escalates to ai tutor`() {
        val cafe = LessonCatalog.findLesson("cafe_order")!!
        val turn1 = cafe.practice.turns[0]

        val feedback = LocalTurnEvaluator.evaluate(turn1, "I would like a warm morning drink")

        assertEquals(EvaluationOutcome.NEEDS_AI_TUTOR, feedback.outcome)
    }

    @Test
    fun `spaced repetition suggests next day first then grows intervals`() {
        val today = 20_000L
        val firstReview = SpacedRepetitionScheduler.computeNextReviewEpochDay(0, today)
        val secondReview = SpacedRepetitionScheduler.computeNextReviewEpochDay(1, today)
        val thirdReview = SpacedRepetitionScheduler.computeNextReviewEpochDay(2, today)
        val fourthReview = SpacedRepetitionScheduler.computeNextReviewEpochDay(3, today)
        val fifthReview = SpacedRepetitionScheduler.computeNextReviewEpochDay(4, today)

        assertEquals(today + 1L, firstReview)
        assertEquals("Mañana", SpacedRepetitionScheduler.formatSuggestedReviewLabelEs(firstReview, today))
        assertEquals(today + 3L, secondReview)
        assertEquals("En 3 días", SpacedRepetitionScheduler.formatSuggestedReviewLabelEs(secondReview, today))
        assertEquals(today + 7L, thirdReview)
        assertEquals(today + 14L, fourthReview)
        assertEquals(today + 30L, fifthReview)
    }

    @Test
    fun `voice comparison creates three blind A B C samples initially hidden`() {
        val state = HablaYaViewModel.createShuffledVoiceComparison()

        assertEquals(3, state.samples.size)
        assertEquals(listOf("Voz A", "Voz B", "Voz C"), state.samples.map { it.blindLabel })
        assertEquals(
            LessonCatalog.voiceProfiles.map { it.id }.toSet(),
            state.samples.map { it.profile.id }.toSet()
        )
        assertFalse(state.isRevealed)
    }

    @Test
    fun `catalog supports scalable CEFR levels from A1 to B1 with boss missions`() {
        val levelsPresent = LessonCatalog.lessons.map { it.level }.toSet()
        assertTrue(CefrLevel.A1 in levelsPresent)
        assertTrue(CefrLevel.A2 in levelsPresent)
        assertTrue(CefrLevel.B1 in levelsPresent)

        val bosses = LessonCatalog.lessons.filter { it.isBossMission }
        assertEquals(3, bosses.size)
        assertEquals(LessonCatalog.lessons.size, LessonCatalog.pocketPhraseCards.size)
    }

    @Test
    fun `blitz deck builds valid rapid-fire rounds with 3 distinct options`() {
        val deck = LessonCatalog.buildBlitzDeck()
        assertTrue(deck.size >= 8)
        deck.forEach { item ->
            assertEquals(3, item.optionsEn.size)
            assertTrue(item.correctOptionEn in item.optionsEn)
        }
    }

    @Test
    fun `player rank scales cleanly with total XP`() {
        val starter = com.example.model.PlayerRank.fromTotalXp(0)
        val explorer = com.example.model.PlayerRank.fromTotalXp(150)
        val ambassador = com.example.model.PlayerRank.fromTotalXp(900)

        assertEquals(1, starter.levelNumber)
        assertEquals(2, explorer.levelNumber)
        assertEquals(5, ambassador.levelNumber)
    }

    @Test
    fun `every lesson variant has NYC location time live reactions and starter sparks`() {
        LessonCatalog.lessons.forEach { lesson ->
            listOf(lesson.practice, lesson.review).forEach { variant ->
                assertTrue(
                    "Missing NYC location for ${variant.titleEs}",
                    variant.nycLocationLabel.isNotBlank()
                )
                assertTrue(
                    "Missing NYC time for ${variant.titleEs}",
                    variant.nycTimeLabel.isNotBlank()
                )
                variant.turns.forEach { turn ->
                    assertTrue(
                        "Missing successReactionEn for ${turn.id}",
                        turn.successReactionEn.isNotBlank()
                    )
                    assertTrue(
                        "Missing starterSparksEn for ${turn.id}",
                        turn.starterSparksEn.isNotEmpty()
                    )
                }
            }
        }
    }
}

