package com.cameraplanner.eventscanner

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cameraplanner.eventscanner.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return // return if context is null

        val dateName = intent?.getStringExtra("DATE_NAME") ?: ""

        // Create a Notification Channel
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, "DATE_REMINDER_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_notification) // change to your notification icon
            .setContentTitle("Reminder")
            .setContentText("Event: $dateName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Show the Notification
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }


    private fun createNotificationChannel(context: Context) {
        // Check if the API level is 26 or higher because the NotificationChannel class is
        // new and not in the support library
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Date Reminder Channel"
            val descriptionText = "Channel for Date Reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("DATE_REMINDER_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
