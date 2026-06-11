package com.example.travelapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class TripReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val tripId = inputData.getInt(KEY_TRIP_ID, -1).takeIf { it != -1 } ?: return Result.success()
        val tripName = inputData.getString(KEY_TRIP_NAME) ?: return Result.success()
        val destination = inputData.getString(KEY_DESTINATION) ?: return Result.success()
        val isTomorrow = inputData.getBoolean(KEY_IS_TOMORROW, true)

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Promemoria viaggi", NotificationManager.IMPORTANCE_HIGH)
        )

        val (title, text) = if (isTomorrow)
            "Partirai domani!" to "$tripName verso $destination inizia domani"
        else
            "Buon viaggio!" to "Oggi inizia $tripName verso $destination"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(if (isTomorrow) tripId * 2 else tripId * 2 + 1, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "trip_reminders_v2"
        const val KEY_TRIP_ID = "trip_id"
        const val KEY_TRIP_NAME = "trip_name"
        const val KEY_DESTINATION = "destination"
        const val KEY_IS_TOMORROW = "is_tomorrow"
    }
}
