package com.yausername.dvd.adapters


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.yausername.dvd.R
import com.yausername.dvd.database.Download
import kotlinx.android.synthetic.main.fragment_downloads.view.*


class DownloadsAdapter : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {

    private var mValues: List<Download> = emptyList()

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
            download_pb.progress = item.downloadedPercent.toInt()
            download_percent_tv.text = "${item.downloadedPercent} %"
            val totalSize = Formatter.formatShortFileSize(context, item.totalSize)
            val downloadedSize = Formatter.formatShortFileSize(context, item.downloadedSize)
            download_size_tv.text = "${downloadedSize}/${totalSize}"
            if (item.mediaType == "audio") {
                format_ic.setImageResource(R.drawable.ic_audio_24dp)
            } else {
                format_ic.setImageResource(R.drawable.ic_video_24dp)
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
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(context, intent, null)
        } else {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}
