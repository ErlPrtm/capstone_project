package com.capstoneproject.aji.data.api

import com.capstoneproject.aji.data.model.Pegawai
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface PegawaiApiService {
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
