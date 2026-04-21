package com.lunaflow.app.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val TAG        = "OpenAIService"
private const val API_URL    = "https://api.openai.com/v1/chat/completions"
private const val MODEL      = "gpt-4o"
private const val MAX_TOKENS = 600

private const val SYSTEM_PROMPT =
    "You are Luna, a warm, knowledgeable, and empathetic menstrual health companion. " +
    "You support teenagers and young adults with accurate, judgment-free health information. " +
    "Always use simple language and relevant emojis. " +
    "End every response with a short encouraging note. " +
    "Never diagnose; always recommend seeing a doctor for serious concerns."

/** Escape a raw string for safe embedding inside a JSON string literal. */
private fun String.jsonEscape(): String = this
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\t", "\\t")

class OpenAIService(private val apiKey: String) : AIService {

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    // Plain Ktor client — no ContentNegotiation plugin needed (we send raw JSON strings)
    private val client = HttpClient(Android)

    // ── AIService impl ────────────────────────────────────────────

    override suspend fun generateWellnessPlan(cycleData: String): String? {
        val prompt = """
            Based on this menstrual cycle data: $cycleData

            Please create a personalized 7-day wellness plan with these sections:
            🥗 Nutrition — What to eat and why
            🏃 Movement — Appropriate exercise for this phase
            🧘 Self-Care — Mental health, sleep, and rest tips
            💊 Supplements — What may help and why
            🌿 Herbal Allies — Teas, adaptogens, or natural remedies

            Keep the tone warm, supportive, and educational.
        """.trimIndent()
        return call(prompt)
    }

    override suspend fun askHealthQuestion(question: String, context: String): String? {
        val prompt = """
            Context about this user: $context

            User's question: $question

            Give a medically accurate, easy-to-understand answer (3-5 sentences).
            Tie your answer to their current cycle phase if relevant.
        """.trimIndent()
        return call(prompt)
    }

    override suspend fun generateDailyTip(phase: String, recentSymptoms: String): String? {
        val prompt = """
            The user is in their $phase phase. Recent symptoms: $recentSymptoms
            Give ONE short actionable health tip (2-3 sentences) for this phase. Use one emoji.
        """.trimIndent()
        return call(prompt)
    }

    // ── Core HTTP call ────────────────────────────────────────────

    private suspend fun call(userPrompt: String): String? {
        return try {
            val body = buildJsonBody(userPrompt)

            Log.d(TAG, "→ POST $API_URL  key=${apiKey.take(20)}…")

            val response = client.post(API_URL) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            val status = response.status
            val raw    = response.bodyAsText()

            Log.d(TAG, "← HTTP ${status.value}  body_preview=${raw.take(400)}")

            if (status != HttpStatusCode.OK) {
                val errorMsg = "API Error ${status.value}: ${raw.take(200)}"
                Log.e(TAG, "OpenAI non-200 error: $errorMsg")
                errorMsg
            } else {
                val content = jsonParser
                    .parseToJsonElement(raw)
                    .jsonObject["choices"]
                    ?.jsonArray
                    ?.firstOrNull()
                    ?.jsonObject?.get("message")
                    ?.jsonObject?.get("content")
                    ?.jsonPrimitive?.content   // .content = unquoted string value
                    ?.trim()

                Log.d(TAG, "Luna says: ${content?.take(100)}")
                content
            }
        } catch (e: Exception) {
            val errorMsg = "API Error: ${e::class.simpleName} — ${e.message}"
            Log.e(TAG, errorMsg, e)
            return errorMsg
        }
    }

    private fun buildJsonBody(userPrompt: String): String =
        """{"model":"$MODEL","max_tokens":$MAX_TOKENS,"temperature":0.7,"messages":[{"role":"system","content":"${SYSTEM_PROMPT.jsonEscape()}"},{"role":"user","content":"${userPrompt.jsonEscape()}"}]}"""
}
