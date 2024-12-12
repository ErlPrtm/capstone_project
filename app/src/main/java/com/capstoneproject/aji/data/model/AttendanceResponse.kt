package com.capstoneproject.aji.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class AttendanceResponse (
    val status: String,
    val message: String,
    val data: AttendanceData?
)

data class AttendanceData (
    val user_id: String,
    val foto: String,
    val similarity_score: Float,
    val similarity_percentage: Float,
    val threshold: Float,
    val input_names: List<String>
)

data class AttendanceLogResponse(
    @SerializedName("data")
    val data: List<AttendanceLog>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)

data class AttendanceLog(
    @SerializedName("log_id")
    val log_id: Int,
    @SerializedName("user_id")
    val user_id: Int,
    @SerializedName("login_time")
    val login_time: String,
    @SerializedName("logout_time")
    val logout_time: String?,
    @SerializedName("tanggal")
    val tanggal: String,
    @SerializedName("status_login")
    val status_login: String,
    @SerializedName("status_logout")
    val status_logout: String
)

data class SalaryParameterResponse(
    val absen: String,
    val insentif: String,
    val lembur: String,
    val parameter_id: Int,
    val posisi: String,
    val telat: String
)

data class CheckoutResponse(
    val status: String,
    val message: String,
    val data: CheckoutData?
)

data class CheckoutData (
    val logout_time: String,
    val overtime_hours: String,
    val status_logout: String
)