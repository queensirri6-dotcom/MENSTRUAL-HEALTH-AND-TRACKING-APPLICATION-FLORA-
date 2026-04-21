package com.lunaflow.app.notification

import kotlinx.datetime.LocalDate

// WASM/JS does not support background tasks or native notifications.
// All notification calls are no-ops on this platform.

actual class NotificationManager {
    actual fun schedulePeriodicReminder(type: ReminderType) { /* no-op on WASM */ }
    actual fun scheduleDateReminder(type: ReminderType, date: LocalDate, daysBeforeOrAfter: Int) { /* no-op */ }
    actual fun cancelReminder(type: ReminderType) { /* no-op */ }
    actual fun cancelAllReminders() { /* no-op */ }
}
