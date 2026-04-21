package com.lunaflow.app.data.remote

/**
 * Cross-platform AI service contract.
 * Android actual: uses com.google.ai.client.generativeai (androidMain)
 * WASM actual: returns fallback strings (wasmJsMain)
 */
expect class GeminiService(apiKey: String) {
    suspend fun generateWellnessPlan(cycleData: String): String?
    suspend fun askHealthQuestion(question: String, context: String): String?
    suspend fun generateDailyTip(phase: String, recentSymptoms: String): String?
}
