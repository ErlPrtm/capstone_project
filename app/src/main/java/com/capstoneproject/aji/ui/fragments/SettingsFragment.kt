package com.capstoneproject.aji.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.R
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.databinding.FragmentSettingsBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.ui.pegawai.PegawaiActivity
import com.capstoneproject.aji.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        homeViewModel = HomeViewModel(RetrofitInstance.api, userPreferences)

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
            val todayDate = getTodayDate()
            val statusAbsence = userPreferences.getStatusAbsence().firstOrNull()
            val lastAbsenceDate = userPreferences.getLastAbsenceData().firstOrNull()

            userPreferences.clear()

            if(!statusAbsence.isNullOrEmpty() && lastAbsenceDate == todayDate) {
                userPreferences.setStatusAbsence(statusAbsence)
                userPreferences.setLastAbsenceData(lastAbsenceDate)
            } else {
                userPreferences.setStatusAbsence("")
            }

            if(!lastAbsenceDate.isNullOrEmpty()) {
                userPreferences.setLastAbsenceData(lastAbsenceDate)
            }

            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            Toast.makeText(requireContext(), getString(R.string.logged_out_message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
