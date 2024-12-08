package com.capstoneproject.aji.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.databinding.FragmentAccountBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.capstoneproject.aji.R

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        checkAuthenticationAndLoadData()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun checkAuthenticationAndLoadData() {
        lifecycleScope.launch {
            userPreferences.getToken().collect { token ->
                if (!token.isNullOrEmpty()) {
                    val fullname = userPreferences.getUserDetail("fullname").first() ?: "Pengguna"
                    val email = userPreferences.getUserDetail("email").first() ?: "info@aji.com"
                    val nip = userPreferences.getUserDetail("nip").first() ?: "123456789"
                    val profileImageUrl =
                        userPreferences.getUserDetail("profile_image").first() ?: ""

                    binding.tvWelcome.text = "Halo, $fullname"
                    binding.tvUsername.text = fullname
                    binding.tvEmail.text = email
                    binding.tvNip.text = "NIP $nip"

                    Glide.with(this@AccountFragment)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(binding.ivAccountProfile)
                } else {
                    redirectToLogin()
                }
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
