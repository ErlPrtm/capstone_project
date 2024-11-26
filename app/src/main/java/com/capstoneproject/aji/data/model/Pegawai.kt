package com.capstoneproject.aji.data.model

data class Pegawai(
    val id_pegawai: Int? = null,
    val user_id: Int,
    val NIK: String? = null,
    val TTL: String? = null, // Format: YYYY-MM-DD
    val alamat: String? = null,
    val jenis_kelamin: String? = null, // "L" atau "P"
    val no_wa: String? = null,
    val no_rek: String? = null,
    val agama: String? = null,
    val posisi: String
)
