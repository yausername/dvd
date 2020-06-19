package com.yausername.dvd.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.yausername.dvd.R
import com.yausername.dvd.vm.VidInfoViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavActivity {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.home_fragment,
                R.id.downloads_fragment,
                R.id.youtube_dl_fragment
            ), drawer_layout
        )
        toolbar.setupWithNavController(navController, appBarConfiguration)
        supportActionBar?.title = navController.currentDestination?.label
        bottom_view?.setupWithNavController(navController)
        nav_view?.setupWithNavController(navController)

        handleIntent(intent)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val navController = Navigation.findNavController(
            this,
            R.id.nav_host_fragment
        )
        val navigated = NavigationUI.onNavDestinationSelected(item!!, navController)
        return navigated || super.onOptionsItemSelected(item)
    }

    override fun hideNav() {
        drawer_layout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        bottom_view?.visibility = View.GONE
    }

    override fun showNav() {
        drawer_layout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        bottom_view?.visibility = View.VISIBLE
    }

    override fun showOptions() {
        toolbar.menu.iterator().forEach { it.isVisible = true }
    }

    override fun hideOptions() {
        toolbar.menu.iterator().forEach { it.isVisible = false }
    }

    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEND == intent.action) {
            navigateHome()
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                if (!URLUtil.isValidUrl(it)) {
                    Toast.makeText(applicationContext, R.string.invalid_url, Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                val vidFormatsVm =
                    ViewModelProvider(this).get(VidInfoViewModel::class.java)
                vidFormatsVm.fetchInfo(it)
            }
        }
    }

    private fun navigateHome() {
        val navController = Navigation.findNavController(
            this,
            R.id.nav_host_fragment
        )
        val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        navController.navigate(R.id.home_fragment, null, navOptions)
    }

}

interface NavActivity {
    fun hideNav()
    fun showNav()
    fun showOptions()
    fun hideOptions()
}
