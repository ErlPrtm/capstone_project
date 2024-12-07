package com.capstoneproject.aji.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.R
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.databinding.ActivityMainBinding
import com.capstoneproject.aji.ui.fragments.*
import com.capstoneproject.aji.ui.login.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(applicationContext)

        lifecycleScope.launch {
            userPreferences.isLoggedIn().collect { isLoggedIn ->
                if (!isLoggedIn) {
                    navigateToLogin()
                    return@collect
                }
            }
        }

        setCurrentFragment(HomeFragment())

        binding.bottomBar.onItemSelected = { position ->
            val fragment: Fragment = when (position) {
                0 -> HomeFragment()
                1 -> AnalyticsFragment()
                2 -> AbsenceFragment()
                3 -> AccountFragment()
                4 -> SettingsFragment()
                else -> HomeFragment()
            }
            setCurrentFragment(fragment)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, fragment)
            .commit()
    }
}
