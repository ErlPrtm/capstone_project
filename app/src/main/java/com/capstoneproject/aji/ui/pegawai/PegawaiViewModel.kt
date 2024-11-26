package com.capstoneproject.aji.ui.pegawai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.model.Pegawai
import com.capstoneproject.aji.data.repository.PegawaiRepository
import kotlinx.coroutines.launch

class PegawaiViewModel(private val repository: PegawaiRepository) : ViewModel() {

    private val _pegawaiList = MutableLiveData<List<Pegawai>>()
    val pegawaiList: LiveData<List<Pegawai>> get() = _pegawaiList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchAllPegawai(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getAllPegawai(token)
                if (response.isSuccessful) {
                    _pegawaiList.postValue(response.body())
                } else {
                    _error.postValue("Failed to fetch data: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun createPegawai(token: String, pegawai: Pegawai) {
        viewModelScope.launch {
            try {
                val response = repository.createPegawai(token, pegawai)
                if (response.isSuccessful) {
                    fetchAllPegawai(token)
                } else {
                    _error.postValue("Failed to create data: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updatePegawai(token: String, id: Int, pegawai: Pegawai) {
        viewModelScope.launch {
            try {
                val response = repository.updatePegawai(token, id, pegawai)
                if (response.isSuccessful) {
                    fetchAllPegawai(token)
                } else {
                    _error.postValue("Failed to update data: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deletePegawai(token: String, id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deletePegawai(token, id)
                if (response.isSuccessful) {
                    fetchAllPegawai(token)
                } else {
                    _error.postValue("Failed to delete data: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            }
        }
    }
}
