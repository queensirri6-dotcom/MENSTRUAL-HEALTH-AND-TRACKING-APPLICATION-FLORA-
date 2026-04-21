package com.lunaflow.app.data.remote

actual class GeminiService actual constructor(apiKey: String) {
    actual suspend fun generateWellnessPlan(cycleData: String): String? {
        return "✨ Simulated Wellness Plan (WASM Target) ✨\n\n- 🥗 Eat healthy greens\n- 🏃 Go for a walk\n- 🧘 Practice mindfulness"
    }

    actual suspend fun askHealthQuestion(question: String, context: String): String? {
        return "I'm running in the browser (WASM). I can't generate AI responses here yet! 🌙"
    }

    actual suspend fun generateDailyTip(phase: String, recentSymptoms: String): String? {
        return "Stay hydrated and listen to your body today! 💧"
    }
}
