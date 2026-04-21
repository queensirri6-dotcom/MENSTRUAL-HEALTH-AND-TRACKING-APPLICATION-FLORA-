package com.lunaflow.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

actual class NotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID_HEALTH = "lunaflow_health"
        const val CHANNEL_ID_REMINDERS = "lunaflow_reminders"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val healthChannel = NotificationChannel(
                CHANNEL_ID_HEALTH,
                "LunaFlow Health Updates",
                AndroidNotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Period, ovulation, and cycle phase alerts" }

            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "LunaFlow Reminders",
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply { description = "Daily logging nudges and medication reminders" }

            val manager = context.getSystemService(AndroidNotificationManager::class.java)
            manager?.createNotificationChannels(listOf(healthChannel, reminderChannel))
        }
    }

    actual fun schedulePeriodicReminder(type: ReminderType) {
        val (tag, delay, unit) = when (type) {
            ReminderType.DAILY_LOG_NUDGE -> Triple(
                "daily_log_nudge", 1L, TimeUnit.DAYS
            )
            ReminderType.WEEKLY_WELLNESS_PLAN -> Triple(
                "weekly_wellness", 7L, TimeUnit.DAYS
            )
            ReminderType.MEDICATION_REMINDER -> Triple(
                "medication_reminder", 1L, TimeUnit.DAYS
            )
            else -> return
        }

        val data = workDataOf("reminder_type" to type.name)
        val request = PeriodicWorkRequestBuilder<LunaReminderWorker>(delay, unit)
            .setInputData(data)
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            tag,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    actual fun scheduleDateReminder(
        type: ReminderType,
        date: LocalDate,
        daysBeforeOrAfter: Int
    ) {
        val targetDate = date.toJavaLocalDate().minusDays(daysBeforeOrAfter.toLong())
        val targetMillis = LocalDateTime.of(targetDate, java.time.LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delayMs = targetMillis - System.currentTimeMillis()
        if (delayMs <= 0) return

        val tag = type.name.lowercase()
        val data = workDataOf("reminder_type" to type.name)
        val request = OneTimeWorkRequestBuilder<LunaReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    actual fun cancelReminder(type: ReminderType) {
        WorkManager.getInstance(context).cancelAllWorkByTag(type.name.lowercase())
    }

    actual fun cancelAllReminders() {
        WorkManager.getInstance(context).cancelAllWork()
    }
}

// ── WorkManager Worker ─────────────────────────────────────────────

class LunaReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val typeName = inputData.getString("reminder_type") ?: return Result.failure()
        val type = try { ReminderType.valueOf(typeName) } catch (e: Exception) { return Result.failure() }

        val (title, body, channelId) = when (type) {
            ReminderType.PERIOD_REMINDER -> Triple(
                "🩸 Period Reminder",
                "Your period is expected in 2 days. Be prepared and be kind to yourself! 💜",
                NotificationManager.CHANNEL_ID_HEALTH
            )
            ReminderType.OVULATION_REMINDER -> Triple(
                "🌕 Fertility Window Alert",
                "You're entering your fertility window. Take note of your body's signals!",
                NotificationManager.CHANNEL_ID_HEALTH
            )
            ReminderType.DAILY_LOG_NUDGE -> Triple(
                "📝 How are you feeling today?",
                "Don't forget to log your symptoms and mood in LunaFlow! 🌸",
                NotificationManager.CHANNEL_ID_REMINDERS
            )
            ReminderType.MEDICATION_REMINDER -> Triple(
                "💊 Medication Reminder",
                "Time to take your daily supplement or medication! 💙",
                NotificationManager.CHANNEL_ID_REMINDERS
            )
            ReminderType.WEEKLY_WELLNESS_PLAN -> Triple(
                "✨ Your Weekly Wellness Plan",
                "Open LunaFlow to get your personalized wellness plan for this week!",
                NotificationManager.CHANNEL_ID_HEALTH
            )
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(AndroidNotificationManager::class.java)
        manager?.notify(type.ordinal, notification)

        return Result.success()
    }
}
