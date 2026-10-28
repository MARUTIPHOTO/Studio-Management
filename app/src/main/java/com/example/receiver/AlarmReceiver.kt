package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "studio_event_alarm_channel_v2"
        const val CHANNEL_NAME = "Studio Event Alarms"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_NAME = "extra_event_name"
        const val EXTRA_EVENT_TIME = "extra_event_time"
        const val EXTRA_EVENT_LOCATION = "extra_event_location"
        const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, System.currentTimeMillis())
        val eventName = intent.getStringExtra(EXTRA_EVENT_NAME) ?: "ઇવેન્ટ શૂટ (Photography Event)"
        val eventTime = intent.getStringExtra(EXTRA_EVENT_TIME) ?: ""
        val eventLocation = intent.getStringExtra(EXTRA_EVENT_LOCATION) ?: ""
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 1. Get the exact default Alarm sound/tone set on the user's device
        val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI

        // 2. Configure NotificationChannel with Alarm audio attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Photography Studio upcoming event alarm alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 600)
                setSound(defaultAlarmUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 3. PendingIntent to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_event_id", eventId)
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val detailText = buildString {
            if (customerName.isNotBlank()) append("ગ્રાહક: $customerName • ")
            if (eventTime.isNotBlank()) append("સમય: $eventTime • ")
            if (eventLocation.isNotBlank()) append("સ્થળ: $eventLocation")
        }.ifBlank { "તમારી ફોટોગ્રાફી ઇવેન્ટ નજીક છે! સમયસર પહોંચવાની તૈયારી કરો." }

        // 4. Build Notification with Alarm priority, fullscreen intent, and device alarm tone
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ ઇવેન્ટ એલાર્મ: $eventName")
            .setContentText(detailText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)
            .setFullScreenIntent(pendingContentIntent, true)
            .setSound(defaultAlarmUri)
            .setVibrate(longArrayOf(0, 600, 300, 600, 300, 600))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            notificationManager.notify(eventId.toInt(), notification)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to display alarm notification", e)
        }

        // 5. Play device's actual alarm tone directly through RingtoneManager with USAGE_ALARM
        try {
            val ringtone = RingtoneManager.getRingtone(context, defaultAlarmUri)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to play alarm ringtone", e)
        }
    }
}
