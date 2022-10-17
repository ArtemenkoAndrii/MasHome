package com.mas.mobile.presentation.activity

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.mas.mobile.R
import com.mas.mobile.appComponent
import com.mas.mobile.repository.SpendingMessageRepository
import com.mas.mobile.service.DateListener
import com.mas.mobile.service.SettingsService
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    private val dateListener = DateListener()
    private lateinit var appBarConfiguration : AppBarConfiguration

    @Inject
    lateinit var messageRepository: SpendingMessageRepository
    @Inject
    lateinit var settingsService: SettingsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.appComponent.injectMainActivity(this)

        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_view) as NavHostFragment).navController
        val graph = navController.navInflater.inflate(R.navigation.nav_graph).also {
            val isThisFirstRun = settingsService.isThisFirstLaunch()
            val dest = if (isThisFirstRun) { R.id.nav_settings } else { R.id.nav_expenditure_list }
            it.setStartDestination(dest)
        }
        navController.graph = graph

        val drawerLayout : DrawerLayout? = findViewById(R.id.drawer_layout)
        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_expenditure_list,
                      R.id.nav_spending_list,
                      R.id.nav_message_list_fragment ,
                      R.id.nav_budget_list),
                drawerLayout)

        setupActionBar(navController, appBarConfiguration)
        setupNavigationMenu(navController)
        setupBottomNavMenu(navController)
        this.applicationContext.registerReceiver(dateListener, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onDestroy() {
        this.applicationContext.unregisterReceiver(dateListener)
        super.onDestroy()
    }

    private fun setupActionBar(navController: NavController, appBarConfig : AppBarConfiguration) {
        setupActionBarWithNavController(navController, appBarConfig)
    }

    private fun setupNavigationMenu(navController: NavController) {
       val sideNavView = findViewById<NavigationView>(R.id.nav_view)
       sideNavView?.setupWithNavController(navController)
    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_bottom_view)
        bottomNav?.setupWithNavController(navController)

        if (bottomNav != null) {
            messageRepository.countUnreadLive().observeForever {
                val badge = bottomNav.getOrCreateBadge(R.id.nav_message_list_fragment)
                if (it > 0) {
                    badge.number = it
                    badge.isVisible = true
                } else {
                    badge.isVisible = false
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val retValue = super.onCreateOptionsMenu(menu)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        if (navigationView == null) {
            menuInflater.inflate(R.menu.bottom_menu, menu)
            return true
        }
        return retValue
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_view))
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_view).navigateUp(appBarConfiguration)
    }
}
