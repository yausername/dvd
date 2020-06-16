package com.yausername.dvd.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.yausername.dvd.R
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class YoutubeDLUpdateWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager


    override suspend fun doWork(): Result {

        createNotificationChannel()
        val notificationId = id.hashCode()
        val notification = NotificationCompat.Builder(applicationContext,
            channelId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Updating youtube-dl")
            .build()

        val foregroundInfo = ForegroundInfo(notificationId, notification)
        setForeground(foregroundInfo)

        val result = YoutubeDL.getInstance().updateYoutubeDL(applicationContext)
        if (result == YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE) {
            withContext(Dispatchers.Main){
                Toast.makeText(applicationContext, "already up to date", Toast.LENGTH_SHORT).show();
            }
        }
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =
                notificationManager?.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW
                )
                notificationChannel.description =
                    channelDescription
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }

    companion object {
        const val channelName = "youtube-dl update"
        const val channelDescription = "youtube-dl update"
        const val channelId = "youtube-dl update"
    }
}

