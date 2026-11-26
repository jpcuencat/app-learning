package com.example.domain

import com.example.model.ConversationTurn
import com.example.model.EvaluationOutcome
import com.example.model.EvaluationSource
import com.example.model.TurnFeedback

/**
 * Pure Kotlin, 100% offline-capable evaluator for everyday English turns.
 * Evaluates common valid English answers locally with rich Spanish explanations,
 * and signals [EvaluationOutcome.NEEDS_AI_TUTOR] when an unexpected/open-ended English answer
 * can benefit from the online Gemini AI tutor.
 */
object LocalTurnEvaluator {

    private val spanishIndicators = listOf(
        "hola", "quiero", "puedo", "donde", "dónde", "esta", "está", "por favor",
        "cuanto", "cuánto", "cuesta", "farmacia", "café", "cafe", "llevar",
        "gracias", "disculpa", "perdón", "perdon", "izquierda", "derecha",
        "estación", "estacion", "desayuno", "maleta", "cuenta", "cita", "mañana"
    )

    private val commonEnglishWords = setOf(
        "i", "you", "we", "me", "my", "the", "a", "an", "is", "are", "it",
        "can", "could", "would", "like", "have", "get", "want", "need",
        "please", "thanks", "thank", "excuse", "sorry", "hello", "hi",
        "where", "how", "much", "what", "when", "time", "turn", "left", "right",
        "go", "here", "for", "to", "yes", "no", "that", "all", "ok", "okay", "sure"
    )

    fun evaluate(turn: ConversationTurn, rawAnswer: String): TurnFeedback {
        val trimmed = rawAnswer.trim()
        if (trimmed.length < 2) {
            return TurnFeedback(
                outcome = EvaluationOutcome.RETRY,
                explanationEs = "Tu respuesta está vacía o es demasiado corta. Escribe o dicta una frase en inglés para este turno.",
                reusableTipEs = turn.hintEs,
                modelExampleEn = turn.modelExampleEn,
                evaluatedText = trimmed,
                source = EvaluationSource.LOCAL_OFFLINE
            )
        }

        val normalized = normalize(trimmed)
        val tokens = normalized.split(" ").filter { it.isNotBlank() }

        // 1. Detect if user responded mostly in Spanish instead of English
        val spanishHits = spanishIndicators.count { indicator ->
            containsPhrase(normalized, normalize(indicator))
        }
        val englishHits = tokens.count { it in commonEnglishWords }
        if (spanishHits >= 1 && englishHits == 0) {
            return TurnFeedback(
                outcome = EvaluationOutcome.RETRY,
                explanationEs = "Parece que respondiste en español. Intenta decir tu respuesta en inglés; puedes escuchar el ejemplo o ver la pista si lo necesitas.",
                reusableTipEs = turn.hintEs,
                modelExampleEn = turn.modelExampleEn,
                evaluatedText = trimmed,
                source = EvaluationSource.LOCAL_OFFLINE
            )
        }

        val criteria = turn.criteria
        val hasPrimary = criteria.requiredAnyKeywords.any { kw ->
            containsPhrase(normalized, normalize(kw))
        }
        val hasSecondary = criteria.secondaryAnyKeywords.isEmpty() ||
            criteria.secondaryAnyKeywords.any { kw ->
                containsPhrase(normalized, normalize(kw))
            }
        val hasPoliteness = !criteria.requirePolitenessForFullPass ||
            criteria.politenessMarkers.any { marker ->
                containsPhrase(normalized, normalize(marker))
            }

        // 2. Full match -> Objective achieved!
        if (hasPrimary && hasSecondary && hasPoliteness) {
            val personalizedExplanation = buildSuccessExplanation(turn, normalized)
            return TurnFeedback(
                outcome = EvaluationOutcome.PASSED,
                explanationEs = personalizedExplanation,
                reusableTipEs = turn.reusableTipEs,
                modelExampleEn = turn.modelExampleEn,
                evaluatedText = trimmed,
                source = EvaluationSource.LOCAL_OFFLINE
            )
        }

        // 3. Has core vocabulary, missing politeness or secondary structure -> Review answer
        if (hasPrimary && !hasSecondary) {
            val hint = criteria.missingSecondaryHintEs.ifBlank {
                "Incluye un poco más de contexto para completar la idea en inglés."
            }
            return TurnFeedback(
                outcome = EvaluationOutcome.REVIEW_ANSWER,
                explanationEs = hint,
                reusableTipEs = turn.hintEs,
                modelExampleEn = turn.modelExampleEn,
                evaluatedText = trimmed,
                source = EvaluationSource.LOCAL_OFFLINE
            )
        }

        if (hasPrimary && !hasPoliteness) {
            return TurnFeedback(
                outcome = EvaluationOutcome.REVIEW_ANSWER,
                explanationEs = criteria.missingPolitenessHintEs,
                reusableTipEs = turn.reusableTipEs,
                modelExampleEn = turn.modelExampleEn,
                evaluatedText = trimmed,
                source = EvaluationSource.LOCAL_OFFLINE
            )
        }

        // 4. Looks like a genuine multi-word English sentence that our local rules don't recognize ->
        // Escalate to online AI tutor if available, otherwise fall back gracefully to hint + retry!
        if (tokens.size >= 3 && englishHits >= 2) {
            return TurnFeedback(
                outcome = EvaluationOutcome.NEEDS_AI_TUTOR,
                explanationEs = "Tu frase está en inglés, pero usa palabras distintas a las esperadas en modo sin conexión. Puedes consultar con el tutor o revisar la pista.",
                reusableTipEs = turn.hintEs,
                modelExampleEn = turn.modelExampleEn,
                evaluatedText = trimmed,
                source = EvaluationSource.LOCAL_OFFLINE
            )
        }

        // 5. Off-topic or incomplete short input
        return TurnFeedback(
            outcome = EvaluationOutcome.RETRY,
            explanationEs = "Todavía no cumple el objetivo del turno: ${turn.taskInstructionEs} Revisa el ejemplo o la pista e inténtalo de nuevo.",
            reusableTipEs = turn.hintEs,
            modelExampleEn = turn.modelExampleEn,
            evaluatedText = trimmed,
            source = EvaluationSource.LOCAL_OFFLINE
        )
    }

    private fun buildSuccessExplanation(turn: ConversationTurn, normalized: String): String {
        return when {
            turn.id == "cafe_p1" && containsPhrase(normalized, "could i have") ->
                "¡Muy bien! \"Could I have...\" es una de las formas más naturales y amables de pedir en inglés, mucho más cordial que decir solo \"Give me\"."
            turn.id == "cafe_p1" && (containsPhrase(normalized, "id like") || containsPhrase(normalized, "i would like")) ->
                "¡Excelente! \"I'd like...\" (I would like) suena educado y muy habitual en cafeterías y restaurantes."
            turn.id == "cafe_p1" && containsPhrase(normalized, "can i get") ->
                "¡Perfecto! \"Can I get...\" es muy común y relajado en cafeterías, especialmente al añadir \"please\"."
            else -> turn.defaultSuccessExplanationEs
        }
    }

    internal fun normalize(input: String): String {
        return input
            .lowercase()
            .replace("'", "")
            .replace("’", "")
            .replace(Regex("[^a-z0-9áéíóúüñ\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun containsPhrase(haystack: String, needle: String): Boolean {
        if (needle.isBlank()) return false
        val paddedHaystack = " $haystack "
        val paddedNeedle = " $needle "
        return paddedHaystack.contains(paddedNeedle)
    }
}
