package com.chaitanya.todoweathermvvmdi.views

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.chaitanya.todoweathermvvmdi.R
import com.chaitanya.todoweathermvvmdi.databinding.ActivityMainBinding
import com.chaitanya.todoweathermvvmdi.viewModel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.itemIconTintList = null

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> navController.navigate(R.id.homeFragment)
                R.id.priority -> navController.navigate(R.id.priorityFragment)
            }
            true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Hide/Show Bottom Navigation
            if (destination.id == R.id.detailsFragment) {
                binding.bottomNavigation.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
            }

            when (destination.id) {
                R.id.homeFragment -> binding.bottomNavigation.menu.findItem(R.id.home)?.isChecked =
                    true
                R.id.priorityFragment -> binding.bottomNavigation.menu.findItem(R.id.priority)?.isChecked =
                    true
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}