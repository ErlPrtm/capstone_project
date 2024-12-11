package com.capstoneproject.aji.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.api.ApiService
import com.capstoneproject.aji.data.model.AttendanceLog
import kotlinx.coroutines.launch

class HomeViewModel(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
    ) : ViewModel() {
    private val _attendanceLog = MutableLiveData<List<AttendanceLog>?>()
    val attendanceLog: LiveData<List<AttendanceLog>?> = _attendanceLog

    fun fetchAttendanceLogs(userId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getAttendanceLogs(userId)

                if(response.isSuccessful) {
                    val attendanceLogResponse = response.body()?.data
                    _attendanceLog.postValue(attendanceLogResponse)
                } else {
                    val errorResponse = response.errorBody()?.string()
                    Log.e("HomeViewModel", "Error fetching attendance history: $errorResponse")

                }
            } catch (e: Exception) {
                throw Exception("Error fetching attendance history: ${e.message}")
            }
        }
    }
}