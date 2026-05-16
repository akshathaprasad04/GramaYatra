package com.gramayatri.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gramayatri.app.R
import com.gramayatri.app.databinding.ActivityMainBinding
import com.gramayatri.app.repository.FirebaseRepository
import com.gramayatri.app.ui.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Seed sample routes on first launch
        FirebaseRepository.seedSampleRoutesIfEmpty()

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        val appBarConfig = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_routes, R.id.nav_ping, R.id.nav_admin)
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            FirebaseRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
