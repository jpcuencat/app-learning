package com.example.model

import androidx.annotation.DrawableRes
import java.util.Locale

enum class CefrLevel(
    val code: String,
    val labelEs: String,
    val fullBadge: String,
    val descriptionEs: String
) {
    A1(
        code = "A1",
        labelEs = "Inicial",
        fullBadge = "A1 · Inicial",
        descriptionEs = "Tu primer día en la ciudad: café, calles y hotel"
    ),
    A2(
        code = "A2",
        labelEs = "Básico",
        fullBadge = "A2 · Básico",
        descriptionEs = "Salidas, restaurantes y pequeños imprevistos"
    ),
    B1(
        code = "B1",
        labelEs = "Intermedio",
        fullBadge = "B1 · Intermedio",
        descriptionEs = "Negocia cambios, horarios y viajes sin estrés"
    )
}

enum class SessionMode {
    PRACTICE,
    REVIEW
}

enum class InputMode {
    SPEAK,
    WRITE
}

enum class CharacterMood {
    NEUTRAL,
    LISTENING,
    THINKING,
    DELIGHTED,
    CONFUSED,
    ENCOURAGING
}

enum class ThemeMode(val storageKey: String, val labelEs: String) {
    AUTO("auto", "Automático"),
    LIGHT("light", "Claro"),
    DARK("dark", "Oscuro");

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: AUTO
    }
}

enum class EvaluationOutcome {
    PASSED,
    REVIEW_ANSWER,
    RETRY,
    NEEDS_AI_TUTOR
}

enum class EvaluationSource {
    LOCAL_OFFLINE,
    AI_TUTOR
}

data class TurnFeedback(
    val outcome: EvaluationOutcome,
    val explanationEs: String,
    val reusableTipEs: String,
    val modelExampleEn: String,
    val evaluatedText: String,
    val source: EvaluationSource,
    val offlineTutorUnavailableNotice: String? = null
)

data class EvaluationCriteria(
    val requiredAnyKeywords: List<String>,
    val secondaryAnyKeywords: List<String> = emptyList(),
    val requirePolitenessForFullPass: Boolean = false,
    val politenessMarkers: List<String> = listOf(
        "please", "could i", "could you", "can i", "can you",
        "would like", "i'd like", "i would like", "excuse me",
        "sorry", "thank", "thanks", "pardon"
    ),
    val missingSecondaryHintEs: String = "",
    val missingPolitenessHintEs: String = "Tu idea se entiende bien, pero añadir una fórmula de cortesía como \"please\", \"could I...\" o \"excuse me\" hará que suene mucho más natural."
)

data class ConversationTurn(
    val id: String,
    val turnNumber: Int,
    val partnerRoleEs: String,
    val partnerLineEn: String,
    val partnerLineEsHelper: String,
    val taskInstructionEs: String,
    val hintEs: String,
    val modelExampleEn: String,
    val defaultSuccessExplanationEs: String,
    val reusableTipEs: String,
    val criteria: EvaluationCriteria,
    val successReactionEn: String = "Great! Got it.",
    val starterSparksEn: List<String> = emptyList()
)

data class ConversationVariant(
    val titleEs: String,
    val sceneGoalEs: String,
    val skillSummaryEs: String,
    val turns: List<ConversationTurn>,
    val nycLocationLabel: String = "Manhattan · NYC",
    val nycTimeLabel: String = "10:00 AM"
)

data class Lesson(
    val id: String,
    val level: CefrLevel,
    val titleEs: String,
    val mapStopLabelEs: String,
    val isBossMission: Boolean = false,
    val practiceSummaryEs: String,
    val reviewSummaryEs: String,
    @DrawableRes val imageRes: Int,
    val practice: ConversationVariant,
    val review: ConversationVariant,
    val rewardPhraseEn: String,
    val rewardPhraseEs: String
)

data class PocketPhraseCard(
    val id: String,
    val lessonId: String,
    val level: CefrLevel,
    val categoryEs: String,
    val phraseEn: String,
    val meaningEs: String
)

data class BlitzRoundItem(
    val id: String,
    val partnerRoleEs: String,
    val partnerLineEn: String,
    val taskInstructionEs: String,
    val correctOptionEn: String,
    val optionsEn: List<String>,
    val turnForFreeEvaluation: ConversationTurn,
    val explanationEs: String
)

data class VoiceProfile(
    val id: String,
    val realName: String,
    val accentLabelEs: String,
    val styleDescriptionEs: String,
    val locale: Locale,
    val pitch: Float,
    val baseRate: Float,
    val preferredVoiceNameKeywords: List<String>
)

data class BlindVoiceSample(
    val blindLabel: String, // "Voz A", "Voz B", "Voz C"
    val profile: VoiceProfile
)

data class LessonProgressSummary(
    val lessonId: String,
    val practiceTurnIndex: Int = 0,
    val practiceCompletedCount: Int = 0,
    val reviewTurnIndex: Int = 0,
    val reviewCompletedCount: Int = 0,
    val starsEarned: Int = 0,
    val bestXp: Int = 0,
    val nextReviewEpochDay: Long? = null,
    val lastUpdatedMillis: Long = 0L
)

data class PlayerRank(
    val titleEs: String,
    val levelNumber: Int,
    val currentLevelXp: Int,
    val nextLevelXpTarget: Int
) {
    val progressFraction: Float
        get() = if (nextLevelXpTarget <= 0) 1f else (currentLevelXp.toFloat() / nextLevelXpTarget).coerceIn(0f, 1f)

    companion object {
        fun fromTotalXp(totalXp: Int): PlayerRank {
            val thresholds = listOf(
                0 to "Turista Curioso",
                120 to "Explorador Urbano",
                280 to "Viajero Seguro",
                500 to "Conversador Ágil",
                800 to "Embajador Global"
            )
            var idx = 0
            for (i in thresholds.indices) {
                if (totalXp >= thresholds[i].first) idx = i
            }
            val base = thresholds[idx].first
            val next = thresholds.getOrNull(idx + 1)?.first ?: (base + 400)
            return PlayerRank(
                titleEs = thresholds[idx].second,
                levelNumber = idx + 1,
                currentLevelXp = (totalXp - base).coerceAtLeast(0),
                nextLevelXpTarget = (next - base).coerceAtLeast(1)
            )
        }
    }
}
