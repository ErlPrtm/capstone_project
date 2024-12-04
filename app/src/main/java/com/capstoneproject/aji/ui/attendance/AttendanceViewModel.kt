package com.capstoneproject.aji.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.model.AttendanceResponse
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
}