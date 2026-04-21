package com.lunaflow.app.domain.model

import androidx.compose.ui.graphics.Color

enum class CyclePhase(val displayName: String) {
    MENSTRUAL("Menstrual"),
    FOLLICULAR("Follicular"),
    OVULATION("Ovulation"),
    LUTEAL("Luteal");

    fun phaseColor(): Color = when (this) {
        MENSTRUAL  -> Color(0xFFE57373) // soft red
        FOLLICULAR -> Color(0xFF81C784) // sage green
        OVULATION  -> Color(0xFFBA68C8) // lavender purple
        LUTEAL     -> Color(0xFF64B5F6) // calm blue
    }

    fun phaseEmoji(): String = when (this) {
        MENSTRUAL  -> "🌑"
        FOLLICULAR -> "🌒"
        OVULATION  -> "🌕"
        LUTEAL     -> "🌖"
    }
}

