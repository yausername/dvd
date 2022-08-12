package org.yausername.dvd.work

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.WorkManager
import com.yausername.youtubedl_android.YoutubeDL
import org.yausername.dvd.App
import org.yausername.dvd.R

private const val TAG = "CancelReceiver"

class CancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val taskId = intent.getStringExtra("taskId")
        val notificationId = intent.getIntExtra("notificationId", 0)
        if (taskId.isNullOrEmpty()) return
        val result = YoutubeDL.getInstance().destroyProcessById(taskId)
        if (result) {
            Log.d(TAG, "Task (id:$taskId) was killed.")
            WorkManager.getInstance(App.context).cancelAllWorkByTag(taskId)
            val notificationManager =
                App.context.getSystemService(Context.NOTIFICATION_SERVICE) as
                        NotificationManager?
            Toast.makeText(App.context, R.string.download_cancelled, Toast.LENGTH_LONG).show()
            notificationManager?.cancel(notificationId)
        }
    }
}