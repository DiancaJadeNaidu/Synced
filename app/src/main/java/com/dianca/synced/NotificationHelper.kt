package com.dianca.synced

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "sync_requests_channel"
    private const val CHANNEL_NAME = "Sync Requests"
    private const val CHANNEL_DESC = "Notifications for new sync requests"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, notifId: Int) {
        // Intent to open SyncRequestsActivity when tapping the notification
        val intent = Intent(context, SyncRequestsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId, // unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification sound
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // replace with your icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // vibration, lights, etc.
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notifId, builder.build())
        }
    }
}
