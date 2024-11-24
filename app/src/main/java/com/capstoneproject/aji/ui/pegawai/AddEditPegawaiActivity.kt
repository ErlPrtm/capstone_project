package com.capstoneproject.aji.ui.pegawai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.capstoneproject.aji.data.model.Pegawai
import com.capstoneproject.aji.databinding.ActivityAddEditPegawaiBinding
import com.capstoneproject.aji.ui.main.PegawaiViewModel

class AddEditPegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPegawaiBinding
    private lateinit var viewModel: PegawaiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(PegawaiViewModel::class.java)

        binding.btnSavePegawai.setOnClickListener {
            val nik = binding.etNIK.text.toString()
            val alamat = binding.etAlamat.text.toString()

            val newPegawai = Pegawai(
                id_pegawai = null,
                NIK = nik,
                TTL = "1995-01-01",
                alamat = alamat,
                jenis_kelamin = "L",
                no_wa = "08123456789",
                no_rek = "1234567890",
                agama = "Islam",
                posisi = "Staff"
            )

            viewModel.createPegawai("Bearer YOUR_TOKEN", newPegawai)
            finish()
        }
    }
}
