package com.capstoneproject.aji.data.repository

import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.Pegawai
import retrofit2.Response

class PegawaiRepository {

    private val api = RetrofitInstance.pegawai

    suspend fun getAllPegawai(token: String): Response<List<Pegawai>> {
        return api.getAllPegawai("Bearer $token")
    }

    suspend fun getPegawaiById(token: String, id: Int): Response<Pegawai> {
        return api.getPegawaiById("Bearer $token", id)
    }

    suspend fun createPegawai(token: String, pegawai: Pegawai): Response<Pegawai> {
        return api.createPegawai("Bearer $token", pegawai)
    }

    suspend fun updatePegawai(token: String, id: Int, pegawai: Pegawai): Response<Pegawai> {
        return api.updatePegawai("Bearer $token", id, pegawai)
    }

    suspend fun deletePegawai(token: String, id: Int): Response<Unit> {
        return api.deletePegawai("Bearer $token", id)
    }
}
