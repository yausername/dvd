package org.yausername.dvd

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.yausername.dvd.work.CancelReceiver

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        AppCompatDelegate.setDefaultNightMode(
            preferences.getString(
                getString(R.string.theme_key),
                AppCompatDelegate.MODE_NIGHT_YES.toString()
            )!!.toInt()
        )
        context = this.applicationContext
        GlobalScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    YoutubeDL.getInstance().init(this@App)
                    FFmpeg.getInstance().init(this@App)
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, R.string.init_failed, Toast.LENGTH_LONG).show()
            }
        }
        registerReceiver(CancelReceiver(), IntentFilter())
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}