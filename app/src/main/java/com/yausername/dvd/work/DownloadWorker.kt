package com.yausername.dvd.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.yausername.dvd.R
import com.yausername.dvd.database.AppDatabase
import com.yausername.dvd.database.Download
import com.yausername.dvd.database.DownloadsRepository
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.*


class DownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager?


    override suspend fun doWork(): Result {

        val url = inputData.getString("url")!!
        val name = inputData.getString("name")!!
        val formatId = inputData.getString("formatId")!!
        val acodec = inputData.getString("acodec")
        val vcodec = inputData.getString ("vcodec")
        val downloadDir = inputData.getString("downloadDir")!!
        val size = inputData.getLong("size", 0L)

        createNotificationChannel()
        val notificationId = id.hashCode()
        val notification = NotificationCompat.Builder(applicationContext,
            channelId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(name)
            .setContentText("Starting download")
            .build()

        val foregroundInfo = ForegroundInfo(notificationId, notification)
        setForeground(foregroundInfo)

        val request = YoutubeDLRequest(url)
        val tmpFile = File.createTempFile("dvd", null, applicationContext.externalCacheDir)
        tmpFile.delete()
        tmpFile.mkdir()
        tmpFile.deleteOnExit()
        request.addOption("-o", "${tmpFile.absolutePath}/%(title)s.%(ext)s")
        val videoOnly = vcodec != "none" && acodec == "none"
        if (videoOnly) {
            request.addOption("-f", "${formatId}+bestaudio")
        } else {
            request.addOption("-f", formatId)
        }

        var destUri: Uri? = null
        try {
            YoutubeDL.getInstance()
                .execute(request, DownloadProgressCallback { progress, etaInSeconds ->
                    showProgress(id.hashCode(), name, progress.toInt(), etaInSeconds)
                })
            val treeUri = Uri.parse(downloadDir)
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val destDir = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            tmpFile.listFiles().forEach {
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension) ?: "*/*"
                destUri = DocumentsContract.createDocument(applicationContext.contentResolver, destDir, mimeType, it.name)
                val ins = it.inputStream()
                val ops = applicationContext.contentResolver.openOutputStream(destUri!!)
                IOUtils.copy(ins, ops)
                IOUtils.closeQuietly(ops)
                IOUtils.closeQuietly(ins)
            }
        } finally {
            tmpFile.deleteRecursively()
        }

        val downloadsDao = AppDatabase.getDatabase(
            applicationContext
        ).downloadsDao()
        val repository =
            DownloadsRepository(downloadsDao)
        val download =
            Download(name, Date().time, size)
        download.downloadedPath = destUri.toString()
        download.downloadedPercent = 100.00
        download.downloadedSize = size
        download.mediaType = if(vcodec == "none" && acodec != "none") "audio" else "video"

        repository.insert(download)

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

