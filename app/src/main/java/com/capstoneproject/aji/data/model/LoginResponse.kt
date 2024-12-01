package com.capstoneproject.aji.data.model

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
    val id: Int,
    val username: String,
    val fullname: String,
    val email: String,
    val role: String,
    val posisi: String
)
