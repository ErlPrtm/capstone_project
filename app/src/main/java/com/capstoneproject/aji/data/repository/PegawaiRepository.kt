package com.capstoneproject.aji.data.repository

import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.Pegawai

class PegawaiRepository {

    suspend fun getAllPegawai(token: String): List<Pegawai> {
        return try {
            val response = RetrofitInstance.api.getAllPegawai(token)
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPegawaiById(token: String, id: Int): Pegawai? {
        return try {
            val response = RetrofitInstance.api.getPegawaiById(token, id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createPegawai(token: String, pegawai: Pegawai): Pegawai? {
        return try {
            val response = RetrofitInstance.api.createPegawai(token, pegawai)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updatePegawai(token: String, id: Int, pegawai: Pegawai): Pegawai? {
        return try {
            val response = RetrofitInstance.api.updatePegawai(token, id, pegawai)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deletePegawai(token: String, id: Int): Boolean {
        return try {
            val response = RetrofitInstance.api.deletePegawai(token, id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

