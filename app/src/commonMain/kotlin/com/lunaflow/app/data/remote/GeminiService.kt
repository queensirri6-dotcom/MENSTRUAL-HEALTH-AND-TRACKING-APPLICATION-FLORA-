package com.lunaflow.app.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeminiService(apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun generateWellnessPlan(cycleData: String): String? {
        val prompt = "Based on this menstrual cycle data: $cycleData, provide a personalized weekly wellness plan for a young girl. Keep it educational, supportive, and focused on nutrition, exercise, and self-care."
        val response = model.generateContent(prompt)
        return response.text
    }

    suspend fun askHealthQuestion(question: String, history: String): String? {
        val prompt = "Context: $history\n\nQuestion: $question\n\nProvide an age-appropriate, medically accurate (but simplified) answer for a teen girl."
        val response = model.generateContent(prompt)
        return response.text
    }
}
