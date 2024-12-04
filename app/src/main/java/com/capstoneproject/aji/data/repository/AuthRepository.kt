package com.capstoneproject.aji.data.repository

import android.util.Log
import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.AttendanceResponse
import com.capstoneproject.aji.data.model.LoginRequest
import com.capstoneproject.aji.data.model.LoginResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

class AuthRepository {

    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return RetrofitInstance.api.login(loginRequest)
    }

    suspend fun absen(
        token: String,
        userId: String,
        fotoFile: File
    ): Response<AttendanceResponse> {
        val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)
        val fotoPart = MultipartBody.Part.createFormData(
            "foto",
            fotoFile.name,
            fotoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        Log.d("AuthRepository", "Mengirim data absensi: Token: Bearer $token, User ID: $userId, Foto: ${fotoFile.name}")
        return RetrofitInstance.api.absen("Bearer $token", userIdPart, fotoPart)
    }
}
