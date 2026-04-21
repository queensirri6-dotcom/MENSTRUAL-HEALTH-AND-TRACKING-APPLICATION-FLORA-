package com.lunaflow.app.data.remote

/**
 * Cross-platform AI service contract.
 * Implementations: GeminiService, OpenAIService
 */
interface AIService {
    suspend fun generateWellnessPlan(cycleData: String): String?
    suspend fun askHealthQuestion(question: String, context: String): String?
    suspend fun generateDailyTip(phase: String, recentSymptoms: String): String?
}
