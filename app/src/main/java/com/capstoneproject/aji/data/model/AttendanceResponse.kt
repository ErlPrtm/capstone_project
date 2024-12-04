package com.capstoneproject.aji.data.model

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