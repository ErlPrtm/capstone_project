package com.capstoneproject.aji.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.api.ApiService
import com.capstoneproject.aji.data.model.SalaryData
import com.capstoneproject.aji.data.model.SalaryParameterResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

class AnalyticsViewModel(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences ?= null
): ViewModel() {
    private val _salaryParameters = MutableLiveData<List<SalaryData>>()
    val salaryParameters: LiveData<List<SalaryData>> get() = _salaryParameters

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val userPosition = userPreferences?.getUserDetail("posisi")?.asLiveData()

    fun fetchSalaryParameter(token: String, parameterId: Int? = null) {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Fetching salary parameters with token: $token, parameterId: $parameterId")
                val response = apiService.getSalaryParameter(token, parameterId)
                _salaryParameters.postValue(listOf(response.data))
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AnalyticsViewModel", "HTTP error: ${e.code()}, $errorBody", e)

                val errorMessage = try {
                    val jsonObject = JSONObject(errorBody)
                    jsonObject.optString("message", "An error Occured")
                } catch (jsonException: JSONException) {
                    Log.e("AnalyticsViewModel", "Failed to parse error JSON", jsonException)
                    "An error Occured"
                }

                _errorMessage.postValue(errorMessage)
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Unexpected error", e)
                _errorMessage.postValue("An unexpected error occured")
            }
        }
    }

}