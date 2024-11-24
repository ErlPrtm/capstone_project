package com.capstoneproject.aji.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.model.Pegawai
import com.capstoneproject.aji.data.repository.PegawaiRepository
import kotlinx.coroutines.launch

class PegawaiViewModel : ViewModel() {

    private val repository = PegawaiRepository()

    private val _pegawaiList = MutableLiveData<List<Pegawai>>()
    val pegawaiList: LiveData<List<Pegawai>> get() = _pegawaiList

    private val _pegawaiDetail = MutableLiveData<Pegawai>()
    val pegawaiDetail: LiveData<Pegawai> get() = _pegawaiDetail

    fun getAllPegawai(token: String) {
        viewModelScope.launch {
            val response = repository.getAllPegawai(token)
            if (true) {
                _pegawaiList.value = response
            }
        }
    }

    fun getPegawaiById(token: String, id: Int) {
        viewModelScope.launch {
            val response = repository.getPegawaiById(token, id)
            if (response != null) {
                _pegawaiDetail.value = response
            }
        }
    }

    fun createPegawai(token: String, pegawai: Pegawai) {
        viewModelScope.launch {
            repository.createPegawai(token, pegawai)
        }
    }

    fun deletePegawai(token: String, id: Int) {
        viewModelScope.launch {
            repository.deletePegawai(token, id)
        }
    }
}
