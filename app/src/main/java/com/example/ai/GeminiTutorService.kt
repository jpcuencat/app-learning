package com.example.ai

import android.util.Log
import com.example.model.ConversationTurn
import com.example.model.EvaluationOutcome
import com.example.model.EvaluationSource
import com.example.model.TurnFeedback
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiTutorService"

interface OnlineTutorService {
    suspend fun evaluateResponse(
        lessonTitle: String,
        sceneGoal: String,
        turn: ConversationTurn,
        userAnswer: String
    ): Result<TurnFeedback>
}

class GeminiTutorService : OnlineTutorService {

    private val evaluationSchema = Schema.obj(
        properties = mapOf(
            "outcome" to Schema.enumeration(
                values = listOf("PASSED", "REVIEW_ANSWER", "RETRY"),
                description = "PASSED if the learner accomplished the communicative task politely and intelligibly in English; REVIEW_ANSWER if mostly understandable but missing politeness or a key detail; RETRY if off-topic, in Spanish, or unclear."
            ),
            "explanationEs" to Schema.string(
                description = "Clear, encouraging explanation in Spanish (2-3 short sentences) explaining why the learner's English phrase works or what should be adjusted."
            ),
            "reusableTipEs" to Schema.string(
                description = "Short reusable tip in Spanish showing how to reuse the expression in other daily situations."
            ),
            "modelExampleEn" to Schema.string(
                description = "Natural everyday English example sentence that fulfills this turn."
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = evaluationSchema
            }
        )
    }

    override suspend fun evaluateResponse(
        lessonTitle: String,
        sceneGoal: String,
        turn: ConversationTurn,
        userAnswer: String
    ): Result<TurnFeedback> {
        val prompt = buildString {
            appendLine("Eres un tutor amable de inglés cotidiano para hispanohablantes.")
            appendLine("Lección: $lessonTitle")
            appendLine("Objetivo de la escena: $sceneGoal")
            appendLine("Lo que dijo la otra persona (inglés): \"${turn.partnerLineEn}\"")
            appendLine("Tarea del estudiante (español): \"${turn.taskInstructionEs}\"")
            appendLine("Frase modelo de referencia: \"${turn.modelExampleEn}\"")
            appendLine("Respuesta del estudiante: \"$userAnswer\"")
            appendLine()
            appendLine("Evalúa si el estudiante cumplió el objetivo del turno en inglés.")
            appendLine("Sé flexible con variaciones naturales de inglés cotidiano, pero exige que responda en inglés y cumpla la intención del turno.")
            appendLine("Responde en español claro, cercano y breve siguiendo el esquema JSON.")
        }

        return try {
            val response = withTimeout(45_000L) {
                model.generateContent(prompt)
            }
            val jsonText = response.text
                ?: return Result.failure(IllegalStateException("El tutor devolvió una respuesta vacía."))
            val json = JSONObject(jsonText)
            val outcomeStr = json.optString("outcome", "REVIEW_ANSWER")
            val outcome = when (outcomeStr) {
                "PASSED" -> EvaluationOutcome.PASSED
                "RETRY" -> EvaluationOutcome.RETRY
                else -> EvaluationOutcome.REVIEW_ANSWER
            }
            val explanation = json.optString("explanationEs", turn.defaultSuccessExplanationEs)
            val reusableTip = json.optString("reusableTipEs", turn.reusableTipEs)
            val modelExample = json.optString("modelExampleEn", turn.modelExampleEn)

            Result.success(
                TurnFeedback(
                    outcome = outcome,
                    explanationEs = explanation,
                    reusableTipEs = reusableTip,
                    modelExampleEn = modelExample,
                    evaluatedText = userAnswer,
                    source = EvaluationSource.AI_TUTOR
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI tutor evaluation timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI tutor evaluation failed", e)
            Result.failure(e)
        }
    }
}
