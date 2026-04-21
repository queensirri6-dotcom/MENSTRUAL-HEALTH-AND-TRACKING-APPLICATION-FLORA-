package com.lunaflow.app.data.remote

import android.util.Log

private const val TAG = "GeminiService"

// Android actual: delegate to OpenAIService so we can swap providers without
// changing commonMain code that expects `GeminiService`.
actual class GeminiService actual constructor(apiKey: String) {
    private val delegate = OpenAIService(apiKey)

    actual suspend fun generateWellnessPlan(cycleData: String): String? {
        return try {
            delegate.generateWellnessPlan(cycleData)
        } catch (e: Exception) {
            val err = "Wrapper Error: ${e.message}"
            Log.e(TAG, err, e)
            err
        }
    }

    actual suspend fun askHealthQuestion(question: String, context: String): String? {
        return try {
            delegate.askHealthQuestion(question, context)
        } catch (e: Exception) {
            val err = "Wrapper Error: ${e.message}"
            Log.e(TAG, err, e)
            err
        }
    }

    actual suspend fun generateDailyTip(phase: String, recentSymptoms: String): String? {
        return try {
            delegate.generateDailyTip(phase, recentSymptoms)
        } catch (e: Exception) {
            val err = "Wrapper Error: ${e.message}"
            Log.e(TAG, err, e)
            err
        }
    }
}
