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

        val fileUri = inputData.getString(fileUriKey)!!
        val fileName = inputData.getString(fileNameKey)!!

        val downloadsDao = AppDatabase.getDatabase(applicationContext).downloadsDao()
        val repository = DownloadsRepository(downloadsDao)

        repository.deleteByUri(fileUri)

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
        const val fileUriKey = "path"
        const val fileNameKey = "name"
    }

}