package com.capstoneproject.aji.ui.pegawai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.capstoneproject.aji.data.model.Pegawai
import com.capstoneproject.aji.databinding.ItemPegawaiBinding

class PegawaiAdapter(
    private val onEdit: (Pegawai) -> Unit,
    private val onDelete: (Pegawai) -> Unit
) : ListAdapter<Pegawai, PegawaiAdapter.PegawaiViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PegawaiViewHolder {
        val binding = ItemPegawaiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PegawaiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PegawaiViewHolder, position: Int) {
        holder.bind(getItem(position), onEdit, onDelete)
    }

    class PegawaiViewHolder(private val binding: ItemPegawaiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pegawai: Pegawai, onEdit: (Pegawai) -> Unit, onDelete: (Pegawai) -> Unit) {
            binding.tvNamaPegawai.text = pegawai.user_id.toString()
            binding.tvPosisiPegawai.text = "Posisi: ${pegawai.posisi}"

            binding.root.setOnClickListener { onEdit(pegawai) }
            binding.root.setOnLongClickListener {
                onDelete(pegawai)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pegawai>() {
            override fun areItemsTheSame(oldItem: Pegawai, newItem: Pegawai): Boolean {
                return oldItem.id_pegawai == newItem.id_pegawai
            }

            override fun areContentsTheSame(oldItem: Pegawai, newItem: Pegawai): Boolean {
                return oldItem == newItem
            }
        }
    }
}
