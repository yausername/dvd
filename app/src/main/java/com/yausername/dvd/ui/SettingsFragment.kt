package com.yausername.dvd.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.yausername.dvd.R
import getPathFromUri

class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val themePreference: ListPreference? = findPreference("Theme")
        themePreference?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                it.entries = arrayOf("Light", "Dark", "Set by Battery Saver");
                it.entryValues = arrayOf(AppCompatDelegate.MODE_NIGHT_NO.toString(), AppCompatDelegate.MODE_NIGHT_YES.toString(), AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY.toString());
            } else{
                it.entries = arrayOf("Light", "Dark", "System Default");
                it.entryValues = arrayOf(AppCompatDelegate.MODE_NIGHT_NO.toString(), AppCompatDelegate.MODE_NIGHT_YES.toString(), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString());
            }
        }
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            AppCompatDelegate.setDefaultNightMode(newValue.toString().toInt())
            true
        }

        val downloadLocationPref: Preference? = findPreference("downloadLocation")
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        downloadLocationPref?.setSummary(sharedPrefs.getString("downloadLocation", "Set default download location"))
        downloadLocationPref?.let {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 6969)
                true
            }
        }
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
                val path = getPathFromUri(data!!.data!!)
                val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                editor.putString("downloadLocation", path).apply()
                findPreference<Preference>("downloadLocation")?.setSummary(path)
            }
        }
    }

}
