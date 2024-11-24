package com.capstoneproject.aji.data.repository

import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.LoginResponse
import retrofit2.Response

class AuthRepository {

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        return RetrofitInstance.api.login(username, password)
    }
}
