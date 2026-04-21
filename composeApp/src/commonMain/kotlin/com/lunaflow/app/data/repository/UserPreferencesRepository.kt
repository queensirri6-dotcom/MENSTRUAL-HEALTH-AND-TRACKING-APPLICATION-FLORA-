package com.lunaflow.app.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.datetime.LocalDate

class UserPreferencesRepository(private val settings: Settings = Settings()) {

    fun saveCyclePreferences(lastPeriodDate: LocalDate, cycleLength: Int, periodLength: Int) {
        settings["lastPeriodDate"] = lastPeriodDate.toString()
        settings["cycleLength"] = cycleLength
        settings["periodLength"] = periodLength
        settings["isOnboardingComplete"] = true
        // Add to cycle history
        val current = getCycleHistory().toMutableList()
        if (!current.contains(lastPeriodDate)) {
            current.add(0, lastPeriodDate)
            saveCycleHistory(current)
        }
    }

    fun getCycleLength(): Int = settings.getInt("cycleLength", 28)
    fun getPeriodLength(): Int = settings.getInt("periodLength", 5)

    fun getLastPeriodDate(): LocalDate? {
        val dateStr = settings.getStringOrNull("lastPeriodDate")
        return dateStr?.let {
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }
    }

    fun isOnboardingComplete(): Boolean = settings.getBoolean("isOnboardingComplete", false)

    // F3 — Cycle length trend history (list of past period start dates)
    fun saveCycleHistory(dates: List<LocalDate>) {
        val encoded = dates.joinToString(",") { it.toString() }
        settings["cycleHistory"] = encoded
    }

    fun getCycleHistory(): List<LocalDate> {
        val raw = settings.getStringOrNull("cycleHistory") ?: return emptyList()
        return raw.split(",").mapNotNull {
            try { LocalDate.parse(it.trim()) } catch (e: Exception) { null }
        }
    }

    fun addPeriodStartDate(date: LocalDate) {
        val current = getCycleHistory().toMutableList()
        if (!current.contains(date)) {
            current.add(0, date)
            if (current.size > 12) current.removeAt(current.size - 1) // keep last 12 cycles
            saveCycleHistory(current)
        }
        settings["lastPeriodDate"] = date.toString()
    }

    // N4 — Medication reminder time (hour:minute stored as "HH:MM")
    fun saveMedicationReminderTime(hour: Int, minute: Int) {
        settings["medicationReminderTime"] = "$hour:$minute"
    }

    fun getMedicationReminderTime(): Pair<Int, Int>? {
        val raw = settings.getStringOrNull("medicationReminderTime") ?: return null
        return try {
            val parts = raw.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) { null }
    }

    // A1 — Daily tip rotation index
    fun getDailyTipIndex(): Int = settings.getInt("dailyTipIndex", 0)
    fun saveDailyTipIndex(index: Int) { settings["dailyTipIndex"] = index }
}

