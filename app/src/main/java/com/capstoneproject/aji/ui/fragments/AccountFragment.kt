package com.capstoneproject.aji.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.databinding.FragmentAccountBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var  userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())

        checkAuthenticationAndLoadData()

        return binding.root
    }

    private fun checkAuthenticationAndLoadData() {
        lifecycleScope.launch {
            userPreferences.getToken().collect{ token ->
                if(!token.isNullOrEmpty()) {
                    val username = extractUsernameFromToken(token)
                    binding.tvUsername.text = username ?: "Pengguna"
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

    private fun extractUsernameFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                val json = JSONObject(payload)
                json.optString("username", "pengguna")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
