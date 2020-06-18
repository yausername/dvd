package com.yausername.dvd.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.yausername.dvd.R
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class CommandWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager?


    override suspend fun doWork(): Result {

        val command = inputData.getString("command")!!

        createNotificationChannel()
        val notificationId = id.hashCode()
        val notification = NotificationCompat.Builder(applicationContext,
            channelId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("youtube-dl command")
            .setContentText("Running command")
            .build()

        val foregroundInfo = ForegroundInfo(notificationId, notification)
        setForeground(foregroundInfo)

        // this is not the recommended way to add options/flags/url and might break in future
        // use the constructor for url, addOption(key) for flags, addOption(key, value) for options
        val request = YoutubeDLRequest(Collections.emptyList())
        val commandRegex = "\"([^\"]*)\"|(\\S+)"
        val m: Matcher = Pattern.compile(commandRegex).matcher(command)
        while (m.find()) {
            if (m.group(1) != null) {
                request.addOption(m.group(1))
            } else {
                request.addOption(m.group(2))
            }
        }

        YoutubeDL.getInstance()
            .execute(request, DownloadProgressCallback { progress, etaInSeconds ->
                showProgress(id.hashCode(), "youtube-dl command", progress.toInt(), etaInSeconds)
            })

        return Result.success()
    }

    private fun showProgress(id: Int, name: String, progress: Int, etaInSeconds: Long) {
        val notification = NotificationCompat.Builder(applicationContext,
            channelId
        )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(name)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Task ?/n (ETA $etaInSeconds seconds)"))
            .setProgress(100, progress, false)
            .build()
        notificationManager?.notify(id, notification)
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
        const val channelName = "dvd download"
        const val channelDescription = "dvd download"
        const val channelId = "dvd download"
    }
}

