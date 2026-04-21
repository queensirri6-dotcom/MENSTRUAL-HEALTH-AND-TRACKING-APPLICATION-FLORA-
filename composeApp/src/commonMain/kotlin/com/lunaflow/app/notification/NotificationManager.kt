package com.lunaflow.app.notification

import kotlinx.datetime.LocalDate

enum class ReminderType {
    PERIOD_REMINDER,        // N1
    OVULATION_REMINDER,     // N2
    DAILY_LOG_NUDGE,        // N3
    MEDICATION_REMINDER,    // N4
    WEEKLY_WELLNESS_PLAN    // N5
}

/**
 * Platform-agnostic notification contract.
 * Android: uses WorkManager + NotificationCompat
 * WASM/JS: no-op
 */
expect class NotificationManager {
    fun schedulePeriodicReminder(type: ReminderType)
    fun scheduleDateReminder(type: ReminderType, date: LocalDate, daysBeforeOrAfter: Int)
    fun cancelReminder(type: ReminderType)
    fun cancelAllReminders()
}
