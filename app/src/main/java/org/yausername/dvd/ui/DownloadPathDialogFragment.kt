package org.yausername.dvd.ui

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import org.yausername.dvd.R
import kotlinx.android.synthetic.main.dialog_fragment_download_path.view.*

class DownloadPathDialogFragment : DialogFragment() {

    private lateinit var listener: DialogListener

    interface DialogListener {
        fun onOk(dialog: DownloadPathDialogFragment)
        fun onFilePicker(dialog: DownloadPathDialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.dialog_fragment_download_path, null)
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val location = sharedPrefs.getString(getString(R.string.download_location_key), null)
            if (location != null) {
                val docId = DocumentsContract.getTreeDocumentId(Uri.parse(location))
                docId?.apply { view.download_path_tv.text = docId }
                    ?: run { view.download_path_tv.text = location }
            } else {
                view.download_path_tv.setText(R.string.val_not_set)
            }
            builder.setView(view)
                .setIcon(R.drawable.ic_folder_24dp)
                .setTitle(R.string.download_location_title)
                .setNegativeButton(R.string.action_choose_folder)
                { dialog, id ->
                    listener.onFilePicker(this)
                }
                .setPositiveButton(android.R.string.ok)
                { dialog, id ->
                    listener.onOk(this)
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as DialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement DialogListener")
            )
        }
    }

}

