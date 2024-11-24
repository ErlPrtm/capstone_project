package com.capstoneproject.aji.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.preferences.UserPreferences
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences(applicationContext)

        lifecycleScope.launch {
            delay(2000)
            userPreferences.getToken().collect { token ->
                if (token != null) {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        }
    }
}
