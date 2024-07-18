package com.example.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // ফাইল আপলোড লজিক
        delay(1000) // উদাহরণস্বরূপ, ১ সেকেন্ডের জন্য ডিলে

        return Result.success()
    }
}
