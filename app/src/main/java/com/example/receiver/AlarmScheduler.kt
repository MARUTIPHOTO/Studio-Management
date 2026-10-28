package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun setAlarmForReminder(
        context: Context,
        timeInMillis: Long,
        eventId: Long = System.currentTimeMillis(),
        eventName: String = "Photography Event",
        customerName: String = "",
        eventTime: String = "",
        eventLocation: String = ""
    ) {
        // If reminder time has already passed according to device clock, don't schedule
        if (timeInMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Skipping past alarm: $timeInMillis <= ${System.currentTimeMillis()}")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(AlarmReceiver.EXTRA_EVENT_NAME, eventName)
            putExtra(AlarmReceiver.EXTRA_CUSTOMER_NAME, customerName)
            putExtra(AlarmReceiver.EXTRA_EVENT_TIME, eventTime)
            putExtra(AlarmReceiver.EXTRA_EVENT_LOCATION, eventLocation)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_event_id", eventId)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Using setAlarmClock ensures it rings at the exact device clock time,
            // bypasses battery saver/Doze, and behaves as a genuine alarm clock.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(timeInMillis, showPendingIntent),
                    pendingIntent
                )
                Log.d(TAG, "AlarmClock set successfully for time: $timeInMillis")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "setAlarmClock failed, falling back to setExactAndAllowWhileIdle", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } catch (ex: Exception) {
                try {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } catch (fallbackEx: Exception) {
                    Log.e(TAG, "Failed to schedule alarm", fallbackEx)
                }
            }
        }
    }

    fun cancelAlarmForReminder(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Alarm cancelled for event $eventId")
        }
    }

    /**
     * Computes the reminder time in millis from event date/time and selected preset option:
     * - "1_day" / "૧ દિવસ પહેલા"
     * - "12_hours" / "૧૨ કલાક પહેલા"
     * - "6_hours" / "૬ કલાક પહેલા"
     * - "3_hours" / "૩ કલાક પહેલા"
     * - "1_hour" / "૧ કલાક પહેલા"
     * - "exact_time" / "ઇવેન્ટ સમયે જ (Exact Time)"
     * - "custom" / "કસ્ટમ સમય"
     */
    fun calculateReminderTimeMillis(
        eventTimeInMillis: Long,
        selectedOption: String,
        customSelectedTimeInMillis: Long = 0L
    ): Long {
        return when (selectedOption.trim()) {
            "1_day", "૧ દિવસ પહેલા", "1 दिन पहले", "1 Day Before" -> {
                eventTimeInMillis - (24 * 60 * 60 * 1000L)
            }
            "12_hours", "૧૨ કલાક પહેલા", "12 घंटे पहले", "12 Hours Before" -> {
                eventTimeInMillis - (12 * 60 * 60 * 1000L)
            }
            "6_hours", "૬ કલાક પહેલા", "6 घंटे પહેલા", "6 Hours Before" -> {
                eventTimeInMillis - (6 * 60 * 60 * 1000L)
            }
            "3_hours", "૩ કલાક પહેલા", "3 घंटे पहले", "3 Hours Before" -> {
                eventTimeInMillis - (3 * 60 * 60 * 1000L)
            }
            "1_hour", "૧ કલાક પહેલા", "1 घंटा पहले", "1 Hour Before" -> {
                eventTimeInMillis - (1 * 60 * 60 * 1000L)
            }
            "exact_time", "ઇવેન્ટ સમયે જ", "ઇવેન્ટ સમયે", "इवेंट के समय", "At Event Time" -> {
                eventTimeInMillis
            }
            "custom", "કસ્ટમ સમય", "कस्टम समय", "Custom" -> {
                if (customSelectedTimeInMillis > 0L) customSelectedTimeInMillis else eventTimeInMillis
            }
            else -> eventTimeInMillis
        }
    }
}
