package org.yausername.dvd.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.apache.commons.io.IOUtils
import org.yausername.dvd.R
import org.yausername.dvd.database.AppDatabase
import org.yausername.dvd.database.Download
import org.yausername.dvd.database.DownloadsRepository
import org.yausername.dvd.utils.FileNameUtils
import java.io.File
import java.util.*

private const val TAG = "DownloadWorker"
class DownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager?


    override suspend fun doWork(): Result {

        val url = inputData.getString(urlKey)!!
        val name = FileNameUtils.createFilename(inputData.getString(nameKey)!!)
        val formatId = inputData.getString(formatIdKey)!!
        val acodec = inputData.getString(acodecKey)
        val vcodec = inputData.getString(vcodecKey)
        val downloadDir = inputData.getString(downloadDirKey)!!
        val size = inputData.getLong(sizeKey, 0L)
        val taskId = inputData.getString(taskIdKey)!!
        val convFormat = inputData.getString(convertFormatKey)!!;

        createNotificationChannel()
        val notificationId = id.hashCode()
        val notification = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(name)
            .setContentText(applicationContext.getString(R.string.download_start))
            .build()

        val foregroundInfo = ForegroundInfo(notificationId, notification)
        setForeground(foregroundInfo)

        val request = YoutubeDLRequest(url)
        val tmpFile = File.createTempFile("dvd", null, applicationContext.externalCacheDir)
        tmpFile.delete()
        tmpFile.mkdir()
        tmpFile.deleteOnExit()
        val videoOnly = vcodec != "none" && acodec == "none"
        if (videoOnly) {
            request.addOption("-f", "${formatId}+bestaudio")
        } else {
            request.addOption("-f", formatId)
        }
        if (convFormat != ""){
            if (vcodec != "none"){
                request.addOption("--format", convFormat);
            } else {
                request.addOption("-x", "");
                request.addOption("--audio-format", convFormat);
            }
            request.addOption("-o", "${tmpFile.absolutePath}/${name}.${convFormat}")
        } else {
            request.addOption("-o", "${tmpFile.absolutePath}/${name}.%(ext)s")
        }

        var destUri: Uri? = null
        try {
            YoutubeDL.getInstance()
                .execute(request, taskId) { progress, _, line ->
                    showProgress(id.hashCode(), taskId, name, progress.toInt(), line, tmpFile)
                }
            val treeUri = Uri.parse(downloadDir)
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val destDir = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            tmpFile.listFiles().forEach {
                val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension) ?: "*/*"
                destUri = DocumentsContract.createDocument(
                    applicationContext.contentResolver,
                    destDir,
                    mimeType,
                    it.name
                )
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
        download.mediaType = if (vcodec == "none" && acodec != "none") "audio" else "video"

        repository.insert(download)

        return Result.success()
    }

    private fun showProgress(
        id: Int,
        taskId: String,
        name: String,
        progress: Int,
        line: String,
        tmpFile: File
    ) {
        val text = line.replace(tmpFile.toString(), "")
        val intent = Intent(applicationContext, CancelReceiver::class.java)
            .putExtra("taskId", taskId)
            .putExtra("notificationId", id)

        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(name)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setProgress(100, progress, progress == -1)
            .addAction(
                R.drawable.baseline_stop_24,
                applicationContext.getString(R.string.cancel_download),
                pendingIntent
            )
            .build()
        notificationManager?.notify(id, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =
                notificationManager?.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                val channelName = applicationContext.getString(R.string.download_noti_channel_name)
                notificationChannel = NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW
                )
                notificationChannel.description =
                    channelName
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }

    companion object {
        private const val channelId = "dvd_download"
        const val urlKey = "url"
        const val nameKey = "name"
        const val formatIdKey = "formatId"
        const val acodecKey = "acodec"
        const val vcodecKey = "vcodec"
        const val downloadDirKey = "downloadDir"
        const val sizeKey = "size"
        const val taskIdKey = "taskId"
        const val convertFormatKey = "convFormat"
    }
}

