package com.capstoneproject.aji.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.R
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.databinding.FragmentSettingsBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.ui.pegawai.PegawaiActivity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        setupListeners()
        setupInitialStates()

        return binding.root
    }

    private fun setupInitialStates() {
        lifecycleScope.launch {
            userPreferences.getRole().collect { role ->
                if (role == "1") {
                    binding.cvDaftarPegawai.visibility = View.VISIBLE
                } else {
                    binding.cvDaftarPegawai.visibility = View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userPreferences.setDarkModeEnabled(isChecked)
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Toast.makeText(requireContext(), getString(R.string.dark_mode_enabled), Toast.LENGTH_SHORT).show()
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Toast.makeText(requireContext(), getString(R.string.dark_mode_disabled), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.cvDaftarPegawai.setOnClickListener {
            val intent = Intent(requireContext(), PegawaiActivity::class.java)
            startActivity(intent)
        }

        binding.ivLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            val statusAbsence = userPreferences.getStatusAbsence().firstOrNull()

            userPreferences.clear()

            if(!statusAbsence.isNullOrEmpty()) {
                userPreferences.setStatusAbsence(statusAbsence)
            }

            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            Toast.makeText(requireContext(), getString(R.string.logged_out_message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
