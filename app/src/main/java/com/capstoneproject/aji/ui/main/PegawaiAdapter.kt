package com.capstoneproject.aji.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.capstoneproject.aji.data.model.Pegawai
import com.capstoneproject.aji.databinding.ItemPegawaiBinding

class PegawaiAdapter(
    private val pegawaiList: List<Pegawai>,
    private val onItemClick: (Pegawai) -> Unit
) : RecyclerView.Adapter<PegawaiAdapter.PegawaiViewHolder>() {

    inner class PegawaiViewHolder(private val binding: ItemPegawaiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pegawai: Pegawai) {
            binding.tvNama.text = pegawai.NIK
            binding.tvPosisi.text = pegawai.posisi
            binding.root.setOnClickListener { onItemClick(pegawai) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PegawaiViewHolder {
        val binding =
            ItemPegawaiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PegawaiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PegawaiViewHolder, position: Int) {
        holder.bind(pegawaiList[position])
    }

    override fun getItemCount(): Int = pegawaiList.size
}
