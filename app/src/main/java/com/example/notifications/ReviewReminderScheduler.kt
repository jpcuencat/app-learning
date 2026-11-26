package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReviewReminderScheduler {

    const val CHANNEL_ID = "hablaya_review_reminders"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    private const val REQUEST_CODE_REMINDER = 4102

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun scheduleReviewReminder(
        context: Context,
        lessonTitleEs: String,
        variantTitleEs: String,
        targetEpochDay: Long,
        reminderHour: Int
    ) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerCal = Calendar.getInstance().apply {
            timeInMillis = TimeUnit.DAYS.toMillis(targetEpochDay)
            set(Calendar.HOUR_OF_DAY, reminderHour.coerceIn(7, 22))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val triggerAt = maxOf(triggerCal.timeInMillis, System.currentTimeMillis() + 60_000L)

        val intent = Intent(context, ReviewReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, "Hora de repasar: $lessonTitleEs")
            putExtra(
                EXTRA_BODY,
                "Practica la variante \"$variantTitleEs\" en 2 minutos para fijar tus frases en inglés."
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        runCatching {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReviewReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun showImmediateNotification(
        context: Context,
        title: String,
        body: String
    ): Boolean {
        ensureChannel(context)
        if (!hasNotificationPermission(context)) return false

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        return runCatching {
            NotificationManagerCompat.from(context).notify(REQUEST_CODE_REMINDER, notification)
            true
        }.getOrDefault(false)
    }
}

class ReviewReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ReviewReminderScheduler.EXTRA_TITLE)
            ?: "Tu repaso diario en HablaYa"
        val body = intent.getStringExtra(ReviewReminderScheduler.EXTRA_BODY)
            ?: "Tienes una conversación lista para repasar hoy en inglés."
        ReviewReminderScheduler.showImmediateNotification(context, title, body)
    }
}
