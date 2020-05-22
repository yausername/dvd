package com.yausername.dvd.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.yausername.dvd.R
import kotlinx.android.synthetic.main.dialog_fragment_download_path.view.*

class DownloadPathDialogFragment: DialogFragment() {

    private lateinit var listener: DialogListener

    interface DialogListener {
        fun onOk(dialog: DownloadPathDialogFragment)
        fun onFilePicker(dialog: DownloadPathDialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;

            val view = inflater.inflate(R.layout.dialog_fragment_download_path, null)
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            view.download_path_tv.text = sharedPrefs.getString("downloadLocation", "Not set")
            builder.setView(view)
                .setIcon(R.drawable.ic_folder_24dp)
                .setTitle("Download location")
                .setNegativeButton("Pick folder",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onFilePicker(this)
                    })
                .setPositiveButton("ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onOk(this)
                    })

            builder.create()
       } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as DialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement DialogListener"))
        }
    }

}

