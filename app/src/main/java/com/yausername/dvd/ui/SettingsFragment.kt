package com.yausername.dvd.ui

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
import com.yausername.youtubedl_android.YoutubeDL

class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val themePreference: ListPreference? = findPreference("Theme")
        themePreference?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                it.entries = arrayOf("Light", "Dark", "Set by Battery Saver")
                it.entryValues = arrayOf(
                    AppCompatDelegate.MODE_NIGHT_NO.toString(),
                    AppCompatDelegate.MODE_NIGHT_YES.toString(),
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY.toString()
                )
            } else {
                it.entries = arrayOf("Light", "Dark", "System Default")
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

        val downloadLocationPref: Preference? = findPreference("downloadLocation")
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        downloadLocationPref?.let {
            val location = sharedPrefs.getString("downloadLocation", null)
            location?.apply { updatePathInSummary(it, this) } ?: run {
                it.summary = "Not set"
            }
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 6969)
                true
            }
        }

        val updateYoutubeDLPref: Preference? = findPreference("updateYoutubeDL")
        updateYoutubeDLPref?.let {
            it.summary = YoutubeDL.getInstance().version(requireContext().applicationContext) ?: "Tap to update"
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                updateYoutubeDL()
                true
            }
        }
    }

    private fun updateYoutubeDL() {
        val workTag = "youtube-dl-update"
        val workManager = WorkManager.getInstance(requireContext().applicationContext)
        val state =
            workManager.getWorkInfosByTag(workTag).get()?.getOrNull(0)?.state
        val running = state === WorkInfo.State.RUNNING || state === WorkInfo.State.ENQUEUED
        if (running) {
            Toast.makeText(
                context,
                "update is already in progress",
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
            "Update queued. Check notification for progress",
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
            6969 -> {
                data?.data?.let {
                    val path = it.toString()
                    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                    editor.putString("downloadLocation", path).apply()
                    findPreference<Preference>("downloadLocation")?.let { preference ->
                        updatePathInSummary(preference,path)
                    }
                }

            }
        }
    }

    private fun updatePathInSummary(preference: Preference, path: String) {
        val docId = DocumentsContract.getTreeDocumentId(Uri.parse(path))
        docId?.apply { preference.summary = docId }
            ?: run { preference.summary = path }
    }

}
