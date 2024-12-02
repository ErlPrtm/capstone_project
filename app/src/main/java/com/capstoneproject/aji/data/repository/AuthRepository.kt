package com.capstoneproject.aji.data.repository

import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.LoginRequest
import com.capstoneproject.aji.data.model.LoginResponse
import retrofit2.Response

class AuthRepository {

    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return RetrofitInstance.api.login(loginRequest)
    }
}
