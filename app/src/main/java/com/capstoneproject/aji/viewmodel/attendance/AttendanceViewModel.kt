package com.capstoneproject.aji.viewmodel.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.model.AttendanceResponse
import com.capstoneproject.aji.data.model.CheckoutResponse
import com.capstoneproject.aji.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.io.File

class AttendanceViewModel(private val authRepository: AuthRepository): ViewModel() {
    fun absen(
        token: String,
        userId: String,
        fotoFile: File,
        onSuccess: (AttendanceResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = authRepository.absen(token, userId, fotoFile)
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("Respons kosong dari server")
                } else {
                    onError("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    fun checkout(
        token: String,
        userId: Int,
        onSuccess: (CheckoutResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = authRepository.checkout(token, userId)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError(response.message() ?: "Checkout failed")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "An error occurred")
            }
        }
    }
}