package com.yausername.dvd.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.work.*
import com.yausername.dvd.R
import com.yausername.dvd.work.CommandWorker
import com.yausername.dvd.work.CommandWorker.Companion.commandKey
import kotlinx.android.synthetic.main.fragment_youtube_dl.*

class YoutubeDlFragment : Fragment(), View.OnClickListener {

    private var command: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_youtube_dl, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    private fun initViews(view: View) {
        command_btn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.command_btn -> {
                command = command_et.text.toString()
                if (isStoragePermissionGranted() && !command.isNullOrBlank()) {
                    startCommand(command!!)
                }
            }
        }
    }

    private fun startCommand(command: String) {
        val workTag = CommandWorker.commandWorkTag
        val workManager = WorkManager.getInstance(activity?.applicationContext!!)
        val state =
            workManager.getWorkInfosByTag(workTag).get()?.getOrNull(0)?.state
        val running = state === WorkInfo.State.RUNNING || state === WorkInfo.State.ENQUEUED
        if (running) {
            Toast.makeText(
                context,
                R.string.command_already_running,
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val workData = workDataOf(
            commandKey to command
        )
        val workRequest = OneTimeWorkRequestBuilder<CommandWorker>()
            .addTag(workTag)
            .setInputData(workData)
            .build()

        workManager.enqueueUniqueWork(
            workTag,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        Toast.makeText(
            context,
            R.string.command_queued,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCommand(command!!)
        }
    }
}
