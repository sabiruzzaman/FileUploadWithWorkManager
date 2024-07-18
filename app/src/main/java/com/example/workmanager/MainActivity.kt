package com.example.workmanager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import com.example.workmanager.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val CHANNEL_ID = "example_channel"
    private val NOTIFICATION_ID = 1
    private val REQUEST_CODE_POST_NOTIFICATIONS = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()



        binding.apply {
            uploadBtn.setOnClickListener {
                if (checkNotificationPermission()) {
                    startFileProcessing(this@MainActivity, "file_example.mp3")
                } else {
                    requestNotificationPermission()
                }
            }

            second.setOnClickListener {

                startActivity(Intent(this@MainActivity, SecondActivity::class.java))

            }

        }

    }


    private fun copyAssetToInternalStorage(
        context: Context,
        assetFileName: String,
        outputFileName: String
    ): File {
        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open(assetFileName)
        val outFile = File(context.filesDir, outputFileName)
        val outputStream = FileOutputStream(outFile)

        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }

        inputStream.close()
        outputStream.flush()
        outputStream.close()

        return outFile
    }


    private fun startFileProcessing(context: Context, assetFileName: String) {
        val copiedFile = copyAssetToInternalStorage(context, assetFileName, "copied_${assetFileName}")

        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>().build()

        val processWorkRequest = OneTimeWorkRequestBuilder<ProcessWorker>()
            .setInputData(workDataOf("file_path" to copiedFile.absolutePath))
            .build()

        WorkManager.getInstance(context)
            .beginWith(uploadWorkRequest)
            .then(processWorkRequest)
            .enqueue()
    }

   /* private fun startFileProcessing(context: Context, assetFileName: String) {
        val copiedFile =
            copyAssetToInternalStorage(context, assetFileName, "copied_${assetFileName}")

        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>().build()

        val processWorkRequest = OneTimeWorkRequestBuilder<ProcessWorker>()
            .setInputData(workDataOf("file_path" to copiedFile.absolutePath))
            .build()

        WorkManager.getInstance(context)
            .beginWith(uploadWorkRequest)
            .then(processWorkRequest)
            .enqueue()

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(processWorkRequest.id)
            .observe(context as LifecycleOwner) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        context.showNotification(
                            "Processing Complete",
                            "File processing completed successfully."
                        )
                    } else {
                        context.showNotification("Processing Failed", "File processing failed.")
                    }
                }
            }
    }
*/

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Example Channel"
            val descriptionText = "This is an example notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun Context.showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
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
                checkNotificationPermission()
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_POST_NOTIFICATIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startFileProcessing(this, "file_example.mp3")
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


}