package com.capstoneproject.aji.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.R
import com.capstoneproject.aji.data.preferences.UserPreferences
import com.capstoneproject.aji.databinding.FragmentSettingsBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import kotlinx.coroutines.flow.collect
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
            userPreferences.isDarkModeEnabled().collect { isDarkModeEnabled ->
                binding.switchDarkMode.isChecked = isDarkModeEnabled
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

        binding.ivLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            userPreferences.clear()
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