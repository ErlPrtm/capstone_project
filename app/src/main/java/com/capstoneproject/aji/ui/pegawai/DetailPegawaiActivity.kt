package com.capstoneproject.aji.ui.pegawai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.capstoneproject.aji.databinding.ActivityDetailPegawaiBinding
import com.capstoneproject.aji.ui.main.PegawaiViewModel

class DetailPegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPegawaiBinding
    private lateinit var viewModel: PegawaiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pegawaiId = intent.getIntExtra("pegawai_id", -1)
        if (pegawaiId == -1) {
            Toast.makeText(this, "Invalid Pegawai ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(PegawaiViewModel::class.java)

        observeDetailPegawai()
        viewModel.getPegawaiById("Bearer YOUR_TOKEN", pegawaiId)

        binding.btnDeletePegawai.setOnClickListener {
            viewModel.deletePegawai("Bearer YOUR_TOKEN", pegawaiId)
            Toast.makeText(this, "Pegawai deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeDetailPegawai() {
        viewModel.pegawaiDetail.observe(this, { pegawai ->
            binding.tvDetailNIK.text = "NIK: ${pegawai.NIK}"
            binding.tvDetailAlamat.text = "Alamat: ${pegawai.alamat}"
        })
    }
}
