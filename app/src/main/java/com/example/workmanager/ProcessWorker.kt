package com.example.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.io.File

class ProcessWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val file = inputData.getString("file_path") ?: return Result.failure()

        val process1Success = processFunction1(File(file))
        val process2Success = processFunction2(File(file))
        val process3Success = processFunction3(File(file))

        return if (process1Success && process2Success && process3Success) {
            sendNotification("Processing Complete", "File processing completed successfully.")
            Result.success()
        } else {
            sendNotification("Processing Failed", "File processing failed.")
            Result.failure()
        }
    }

    private suspend fun processFunction1(file: File): Boolean {
        // প্রথম প্রসেসিং ফাংশনের লজিক
        delay(2000) // উদাহরণস্বরূপ, ১ সেকেন্ডের জন্য ডিলে
        return true
    }

    private suspend fun processFunction2(file: File): Boolean {
        // দ্বিতীয় প্রসেসিং ফাংশনের লজিক
        delay(2000) // উদাহরণস্বরূপ, ১ সেকেন্ডের জন্য ডিলে
        return true
    }

    private suspend fun processFunction3(file: File): Boolean {
        // তৃতীয় প্রসেসিং ফাংশনের লজিক
        delay(2000) // উদাহরণস্বরূপ, ১ সেকেন্ডের জন্য ডিলে
        return true
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default_channel_id"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .build()

        notificationManager.notify(1, notification)
    }
}
