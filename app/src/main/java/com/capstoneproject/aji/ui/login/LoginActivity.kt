package com.capstoneproject.aji.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.LoginRequest
import com.capstoneproject.aji.data.model.LoginResponse
import com.capstoneproject.aji.data.preferences.UserPreferences
import com.capstoneproject.aji.databinding.ActivityLoginBinding
import com.capstoneproject.aji.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreferences: UserPreferences

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
                performLogin(username, password)
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val requestBody = LoginRequest(username, password)

                val response = RetrofitInstance.api.login(requestBody)

                Log.d("Perform Login", "Request: $username, $password")
                Log.d("Perform Login", "Response: ${response.message()}")

                if (response.isSuccessful) {
                    val loginResponse: LoginResponse? = response.body()
                    val token = loginResponse?.data?.token
                    val role = loginResponse?.data?.user?.role

                    if (token != null && role != null) {
                        userPreferences.saveToken(token)
                        userPreferences.setLoggedIn(true)
                        userPreferences.saveRole(role)

                        navigateToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "Token atau role tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Gagal gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
