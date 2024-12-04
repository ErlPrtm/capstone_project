package com.capstoneproject.aji.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String,
    val data: LoginData? = null
)

data class LoginData(
    val token: String,
    val user: User
)

data class User(
    val user_id: Int,
    val username: String,
    val email: String,
    val fullname: String,
    val role: String,
    val posisi: String
)
