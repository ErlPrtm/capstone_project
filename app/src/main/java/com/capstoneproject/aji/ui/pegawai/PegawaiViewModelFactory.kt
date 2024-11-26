package com.capstoneproject.aji.ui.pegawai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.capstoneproject.aji.data.repository.PegawaiRepository

class PegawaiViewModelFactory(private val repository: PegawaiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PegawaiViewModel::class.java)) {
            return PegawaiViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
