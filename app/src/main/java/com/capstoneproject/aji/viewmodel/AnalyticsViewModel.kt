package com.capstoneproject.aji.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.api.ApiService
import com.capstoneproject.aji.data.model.SalaryData
import com.capstoneproject.aji.data.model.SalaryParameterResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences ?= null
): ViewModel() {
    private val _salaryParameters = MutableLiveData<List<SalaryData>>()
    val salaryParameters: LiveData<List<SalaryData>> get() = _salaryParameters

    fun fetchSalaryParameter(token: String, parameterId: Int ?= null) {
        viewModelScope.launch {
            try {
                val response = apiService.getSalaryParameter(token, parameterId)

                _salaryParameters.postValue(listOf(response.data))
            } catch(e: Exception) {
                Log.e("AnalyticsViewModel", "Failed to fetch salary parameters", e)
            }
        }
    }
}