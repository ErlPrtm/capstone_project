package com.capstoneproject.aji.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.preferences.UserPreferences
import com.capstoneproject.aji.data.repository.AuthRepository
import com.capstoneproject.aji.databinding.ActivityLoginBinding
import com.capstoneproject.aji.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreferences: UserPreferences
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(applicationContext)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(username, password, { token ->
                    lifecycleScope.launch {
                        userPreferences.saveToken(token)
                        userPreferences.setLoggedIn(true)
                        navigateToMain()
                    }
                }, { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
