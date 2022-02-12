package org.yausername.dvd.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.startActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import kotlinx.android.synthetic.main.fragment_downloads.view.*
import org.yausername.dvd.R
import org.yausername.dvd.database.Download
import org.yausername.dvd.work.DeleteWorker

class DownloadsAdapter : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {

    private var mValues: List<Download> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<Download>) {
        mValues = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_downloads, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        with(holder.itemView) {
            title_tv.text = item.name
            @SuppressLint("SetTextI18n")
            download_percent_tv.text = "${item.downloadedPercent}%"
            val totalSize = Formatter.formatShortFileSize(context, item.totalSize)
            val downloadedSize = Formatter.formatShortFileSize(context, item.downloadedSize)
            @SuppressLint("SetTextI18n")
            download_size_tv.text = "${downloadedSize}/${totalSize}"
            if (item.mediaType == "audio") {
                format_ic.setImageResource(R.drawable.ic_baseline_audiotrack_24)
            } else {
                format_ic.setImageResource(R.drawable.ic_baseline_video_library_24)
            }
            item_more.setOnClickListener {
                val popupMenu = PopupMenu(context, holder.itemView)
                val inflater = popupMenu.menuInflater
                inflater.inflate(R.menu.sub_menu, popupMenu.menu)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.share -> {
                            shareContent(item.downloadedPath, context)
                        }
                        R.id.delete -> {
                            startDelete(item.id, context)
                        }
                    }
                    true
                }
            }
            setOnClickListener {
                viewContent(item.downloadedPath, it.context)
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun viewContent(path: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(path)
        val downloadedFile = DocumentFile.fromSingleUri(context, uri)!!
        if (!downloadedFile.exists()) {
            Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show()
            return
        }
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(context, intent, null)
        } else {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareContent(path: String, context: Context) {
        val file = DocumentFile.fromSingleUri(context, Uri.parse(path))!!
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = context.contentResolver.getType(Uri.parse(path)) ?: "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(context, Intent.createChooser(intent, null), null)
        } else {
            Toast.makeText(context, R.string.file_cannot_be_shared, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startDelete(id: Long, context: Context) {
        val workTag = "tag_$id"
        val workManager = WorkManager.getInstance(context.applicationContext!!)
        val state = workManager.getWorkInfosByTag(workTag).get()?.getOrNull(0)?.state

        if (state === WorkInfo.State.RUNNING || state === WorkInfo.State.ENQUEUED) {
            return
        }

        val workData = workDataOf(
            DeleteWorker.fileIdKey to id
        )
        val workRequest = OneTimeWorkRequestBuilder<DeleteWorker>()
            .addTag(workTag)
            .setInputData(workData)
            .build()

        workManager.enqueueUniqueWork(
            workTag,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

}