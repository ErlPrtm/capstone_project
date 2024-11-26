package com.capstoneproject.aji.ui.pegawai

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstoneproject.aji.R
import com.capstoneproject.aji.data.model.Pegawai
import com.capstoneproject.aji.data.repository.PegawaiRepository
import com.capstoneproject.aji.databinding.ActivityPegawaiBinding
import com.capstoneproject.aji.databinding.DialogAddEditPegawaiBinding

class PegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPegawaiBinding
    private lateinit var viewModel: PegawaiViewModel
    private lateinit var adapter: PegawaiAdapter

    // Assuming you have a repository instance
    private lateinit var repository: PegawaiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your repository here
        repository = PegawaiRepository() // Replace this with your actual repository initialization

        // Use the factory to create the ViewModel
        val factory = PegawaiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PegawaiViewModel::class.java]

        adapter = PegawaiAdapter(
            onEdit = { pegawai -> showAddEditDialog(pegawai) },
            onDelete = { pegawai -> viewModel.deletePegawai("your_token_here", pegawai.id_pegawai ?: 0) }
        )

        binding.rvPegawai.layoutManager = LinearLayoutManager(this)
        binding.rvPegawai.adapter = adapter

        binding.btnAddPegawai.setOnClickListener { showAddEditDialog() }

        observeViewModel()
        viewModel.fetchAllPegawai("your_token_here")
    }

    private fun observeViewModel() {
        viewModel.pegawaiList.observe(this) { list: List<Pegawai> ->
            adapter.submitList(list)
        }

        viewModel.error.observe(this) { error: String ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddEditDialog(pegawai: Pegawai? = null) {
        val dialogBinding = DialogAddEditPegawaiBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (pegawai == null) "Tambah Pegawai" else "Edit Pegawai")
            .setView(dialogBinding.root)
            .create()

        pegawai?.let {
            dialogBinding.etUserId.setText(it.user_id.toString())
            dialogBinding.etPosisi.setText(it.posisi)
        }

        dialogBinding.btnSave.setOnClickListener {
            val userId = dialogBinding.etUserId.text.toString().toIntOrNull()
            val posisi = dialogBinding.etPosisi.text.toString()

            if (userId != null && posisi.isNotEmpty()) {
                if (pegawai == null) {
                    viewModel.createPegawai("your_token_here", Pegawai(user_id = userId, posisi = posisi))
                } else {
                    viewModel.updatePegawai("your_token_here", pegawai.id_pegawai ?: 0, Pegawai(user_id = userId, posisi = posisi))
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "User  ID dan Posisi harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
