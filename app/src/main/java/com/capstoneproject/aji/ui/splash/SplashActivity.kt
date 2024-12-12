package com.capstoneproject.aji.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.R
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.databinding.ActivitySplashBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
    //  Pasang splash screen bawaan
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen()
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_splash)

        val userPreferences = UserPreferences(applicationContext)

        lifecycleScope.launch {
            delay(2000)
            binding.progressBar.visibility = View.VISIBLE
            userPreferences.getToken().collect { token ->
                val intent = if (token != null) {
                    Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    Intent(this@SplashActivity, LoginActivity::class.java)
                }
                startActivity(intent)
                binding.progressBar.visibility = View.GONE
                finish()
            }
        }
    }
}
