package com.yausername.dvd.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.yausername.dvd.R
import com.yausername.dvd.work.YoutubeDLUpdateWorker
import com.yausername.dvd.work.YoutubeDLUpdateWorker.Companion.workTag
import com.yausername.youtubedl_android.YoutubeDL


class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val themePreference: ListPreference? = findPreference(getString(R.string.theme_key))
        themePreference?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                it.entries = arrayOf(
                    getString(R.string.theme_light), getString(R.string.theme_dark), getString(
                        R.string.theme_battery_saver
                    )
                )
                it.entryValues = arrayOf(
                    AppCompatDelegate.MODE_NIGHT_NO.toString(),
                    AppCompatDelegate.MODE_NIGHT_YES.toString(),
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY.toString()
                )
            } else {
                it.entries = arrayOf(
                    getString(R.string.theme_light), getString(R.string.theme_dark), getString(
                        R.string.theme_default
                    )
                )
                it.entryValues = arrayOf(
                    AppCompatDelegate.MODE_NIGHT_NO.toString(),
                    AppCompatDelegate.MODE_NIGHT_YES.toString(),
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
                )
            }
        }
        themePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt())
                true
            }

        val downloadLocationPref: Preference? = findPreference(getString(R.string.download_location_key))
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        downloadLocationPref?.let {
            val location = sharedPrefs.getString(getString(R.string.download_location_key), null)
            location?.apply { updatePathInSummary(it, this) } ?: it.setSummary(R.string.val_not_set)
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                openDirectoryChooser()
                true
            }
        }

        val updateYoutubeDLPref: Preference? = findPreference(getString(R.string.youtubedl_update_key))
        updateYoutubeDLPref?.let {
            it.summary = YoutubeDL.getInstance().version(requireContext().applicationContext) ?: getString(
                            R.string.action_update)
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                updateYoutubeDL()
                true
            }
        }
    }

    private fun openDirectoryChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE)
    }

    private fun updateYoutubeDL() {
        val workManager = WorkManager.getInstance(requireContext().applicationContext)
        val state =
            workManager.getWorkInfosByTag(workTag).get()?.getOrNull(0)?.state
        val running = state === WorkInfo.State.RUNNING || state === WorkInfo.State.ENQUEUED
        if (running) {
            Toast.makeText(
                context,
                R.string.update_already_running,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val workRequest = OneTimeWorkRequestBuilder<YoutubeDLUpdateWorker>()
            .addTag(workTag)
            .build()

        workManager.enqueueUniqueWork(
            workTag,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        Toast.makeText(
            context,
            R.string.update_queued,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? NavActivity)?.hideNav()
        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? NavActivity)?.showNav()
        (activity as? NavActivity)?.showOptions()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        (activity as? NavActivity)?.hideOptions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPEN_DIRECTORY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        activity?.contentResolver?.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        updateDefaultDownloadLocation(it.toString())
                    }
                }
            }
        }
    }

    private fun updateDefaultDownloadLocation(path: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(getString(R.string.download_location_key), path).apply()
        findPreference<Preference>(getString(R.string.download_location_key))?.let { preference ->
            updatePathInSummary(preference,path)
        }
    }

    private fun updatePathInSummary(preference: Preference, path: String) {
        val docId = DocumentsContract.getTreeDocumentId(Uri.parse(path))
        docId?.apply { preference.summary = docId }
            ?: run { preference.summary = path }
    }

    companion object {
        private const val OPEN_DIRECTORY_REQUEST_CODE = 42070
    }
}
