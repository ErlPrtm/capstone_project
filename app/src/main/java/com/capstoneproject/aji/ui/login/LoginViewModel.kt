package com.capstoneproject.aji.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    fun login(
        username: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.login(username, password)
                if (response.isSuccessful) {
                    val token = response.body()?.data?.token
                    if (!token.isNullOrEmpty()) {
                        onSuccess(token)
                    } else {
                        onError("Token tidak ditemukan.")
                    }
                } else {
                    onError("Login gagal: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan: ${e.message ?: "Unknown error"}")
            }
        }
    }
}
