package org.yausername.dvd.work

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yausername.dvd.database.AppDatabase
import org.yausername.dvd.database.DownloadsRepository

class DeleteWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {

        val fileId = inputData.getLong(fileIdKey, 0)

        val downloadsDao = AppDatabase.getDatabase(applicationContext).downloadsDao()
        val repository = DownloadsRepository(downloadsDao)
        val download = downloadsDao.getById(fileId)

        val fileName = download.name
        val fileUri = download.downloadedPath

        repository.delete(download)

        val file = DocumentFile.fromSingleUri(applicationContext, Uri.parse(fileUri))!!
        if (file.exists()) {
            file.delete()
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Deleted $fileName", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return Result.success()
    }

    companion object {
        const val fileIdKey = "id"
    }

}