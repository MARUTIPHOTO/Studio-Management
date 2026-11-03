package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.model.EventBooking
import com.example.ui.strings.AppLanguage
import com.example.ui.strings.AppStrings

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "studio_event_alarm_channel_v2"
        const val CHANNEL_NAME = "Studio Event Alarms"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_NAME = "extra_event_name"
        const val EXTRA_EVENT_TIME = "extra_event_time"
        const val EXTRA_EVENT_LOCATION = "extra_event_location"
        const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
        const val EXTRA_CUSTOMER_MOBILE = "extra_customer_mobile"

        const val ACTION_STOP_ALARM = "com.example.receiver.ACTION_STOP_ALARM"

        @Volatile
        private var activeRingtone: Ringtone? = null

        fun stopAlarm(context: Context, eventId: Long? = null) {
            try {
                activeRingtone?.stop()
                activeRingtone = null
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error stopping ringtone", e)
            }
            if (eventId != null && eventId > 0) {
                try {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.cancel(eventId.toInt())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)

        // 1. If this is an action to STOP / Dismiss the alarm
        if (intent.action == ACTION_STOP_ALARM) {
            stopAlarm(context, if (eventId > 0) eventId else null)
            return
        }

        // 2. Safety Check: Verify that the event still exists in the local database and reminder is still active
        if (eventId > 0) {
            try {
                val db = AppDatabase.getInstance(context)
                val event = db.eventDao().getEventByIdDirect(eventId)
                if (event == null || !event.reminderEnabled || event.status == EventBooking.STATUS_COMPLETED) {
                    Log.d("AlarmReceiver", "Event $eventId was deleted or reminder disabled. Skipping alarm.")
                    return
                }
                // Verify that the customer still exists in the database
                val customer = db.customerDao().getCustomerByIdDirect(event.customerId)
                if (customer == null) {
                    Log.d("AlarmReceiver", "Customer ${event.customerId} was deleted from app. Skipping alarm.")
                    return
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error validating event/customer existence", e)
            }
        }

        val eventName = intent.getStringExtra(EXTRA_EVENT_NAME) ?: "ઇવેન્ટ શૂટ"
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: ""
        val customerMobile = intent.getStringExtra(EXTRA_CUSTOMER_MOBILE) ?: ""
        val eventTime = intent.getStringExtra(EXTRA_EVENT_TIME) ?: ""
        val eventLocation = intent.getStringExtra(EXTRA_EVENT_LOCATION) ?: ""

        // 3. Read current language from shared preferences
        val langCode = context.getSharedPreferences("maruti_studio_prefs", Context.MODE_PRIVATE)
            .getString("app_language", AppLanguage.GUJARATI.name) ?: AppLanguage.GUJARATI.name
        val currentLang = try { AppLanguage.valueOf(langCode) } catch (e: Exception) { AppLanguage.GUJARATI }
        val strings = AppStrings.get(currentLang)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 4. Default Alarm URI
        val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI

        // 5. Setup Notification Channel
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

        // 6. Action Intent: "🛑 Stop Alarm" button right on notification card
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val pendingStopIntent = PendingIntent.getBroadcast(
            context,
            (eventId + 100000).toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            strings.alarmStopAction,
            pendingStopIntent
        ).build()

        // 7. Delete Intent: If user swipes away the notification, stop the ringtone immediately
        val deleteIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val pendingDeleteIntent = PendingIntent.getBroadcast(
            context,
            (eventId + 200000).toInt(),
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 8. Content Intent: Tapping notification opens MainActivity and silences alarm
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_event_id", eventId)
            putExtra("stop_alarm", true)
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 9. Format Customer & Event details text for notification card
        val detailText = buildString {
            if (customerName.isNotBlank()) append("${strings.alarmCustomerPrefix}: $customerName\n")
            if (customerMobile.isNotBlank()) append("${strings.alarmMobilePrefix}: $customerMobile\n")
            if (eventTime.isNotBlank()) append("${strings.alarmTimePrefix}: $eventTime\n")
            if (eventLocation.isNotBlank()) append("${strings.alarmLocationPrefix}: $eventLocation")
        }.trim().ifBlank { "તમારી ફોટોગ્રાફી ઇવેન્ટ નજીક છે! સમયસર પહોંચવાની તૈયારી કરો." }

        // 10. Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("${strings.alarmNotificationTitle}: $eventName")
            .setContentText(detailText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)
            .setDeleteIntent(pendingDeleteIntent)
            .setFullScreenIntent(pendingContentIntent, true)
            .addAction(stopAction)
            .setSound(defaultAlarmUri)
            .setVibrate(longArrayOf(0, 600, 300, 600, 300, 600))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            notificationManager.notify(eventId.toInt(), notification)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to display alarm notification", e)
        }

        // 11. Play alarm sound via RingtoneManager and store active reference for stopping
        try {
            activeRingtone?.stop()
            val ringtone = RingtoneManager.getRingtone(context, defaultAlarmUri)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
                activeRingtone = ringtone
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to play alarm ringtone", e)
        }
    }
}
