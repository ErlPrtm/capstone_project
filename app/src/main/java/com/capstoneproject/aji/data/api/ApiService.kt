package com.capstoneproject.aji.data.api

import com.capstoneproject.aji.data.model.AttendanceResponse
import com.capstoneproject.aji.data.model.LoginRequest
import com.capstoneproject.aji.data.model.LoginResponse
import com.capstoneproject.aji.data.model.Pegawai
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/users/login")
    suspend fun login(
        @Body requestBody: LoginRequest
    ): Response<LoginResponse>

    @Multipart
    @POST("api/log_kehadiran/absen")
    suspend fun absen(
        @Header("Authorization") token: String,
        @Part("user_id") userId: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<AttendanceResponse>

    @GET("api/data_pegawai")
    suspend fun getAllPegawai(
        @Header("Authorization") token: String
    ): Response<List<Pegawai>>

    @GET("api/data_pegawai/{id}")
    suspend fun getPegawaiById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Pegawai>

    @POST("api/data_pegawai")
    suspend fun createPegawai(
        @Header("Authorization") token: String,
        @Body pegawai: Pegawai
    ): Response<Pegawai>

    @PUT("api/data_pegawai/{id}")
    suspend fun updatePegawai(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body pegawai: Pegawai
    ): Response<Pegawai>

    @DELETE("api/data_pegawai/{id}")
    suspend fun deletePegawai(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}
