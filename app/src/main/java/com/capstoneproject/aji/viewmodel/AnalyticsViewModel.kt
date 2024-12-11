package com.capstoneproject.aji.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.api.ApiService
import com.capstoneproject.aji.data.model.SalaryParameterResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences ?= null
): ViewModel() {
    private val _salaryParameters = MutableLiveData<SalaryParameterResponse>()
    val salaryParameters: LiveData<SalaryParameterResponse> get() = _salaryParameters

    suspend fun getToken(): String? {
        return userPreferences?.getToken()?.firstOrNull()
    }

    fun fetchSalaryParameter(parameterId: Int ?= null) {
        viewModelScope.launch {
            val token = getToken()
            if(token != null) {
                try {
                    val response = apiService.getSalaryParameter("Bearer $token", parameterId)
                    _salaryParameters.postValue(response)
                } catch(e: Exception) {
                    Log.e("AnalyticsViewModel", "Failed to fetch salary parameters", e)
                }
            } else {
                Log.e("AnalyticsViewModel", "Token missing")
            }
        }
    }
}