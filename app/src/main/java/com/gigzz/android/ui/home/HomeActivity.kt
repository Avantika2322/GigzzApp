package com.gigzz.android.ui.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.gigzz.android.MyFirebaseMessagingService
import com.gigzz.android.R
import com.gigzz.android.databinding.ActivityHomeBinding
import com.gigzz.android.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private var isFromConnection: Boolean = false
    var isPendingIntent: Boolean = false
    var notificationType: String? = null

    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        notificationType = MyFirebaseMessagingService.notiType
        isPendingIntent = MyFirebaseMessagingService.pendingIntent
        isFromConnection = intent.getBooleanExtra("connection", false)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupWithNavController(binding.bottomNavView, navController)

        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.home_nav_graph)
        val graphDestination = if (isFromConnection) {
            R.id.connectionFragment
        } else {
            if (notificationType == "8") {
                R.id.chatFragment
            } else R.id.homeFragment
        }
        navGraph.setStartDestination(graphDestination)
        navController.graph = navGraph

        val topLevelDestinationIds: Set<Int> = setOf(
            R.id.homeFragment, R.id.connectionFragment, R.id.chatFragment, R.id.jobsFragment
        )
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            binding.bottomAppBar.isVisible = topLevelDestinationIds.contains(destination.id)
            binding.fab.isVisible = topLevelDestinationIds.contains(destination.id)
        }
        binding.fab.setOnClickListener {
            navController.navigate(R.id.addPostFragment)
            /*when (userType) {
                1 -> navController.navigate(R.id.postTextFragment)
                2 -> navController.navigate(R.id.fragmentPostEditJob)
                3 -> navController.navigate(R.id.postedJobByCompanyFragment)
            }*/
        }
    }
}