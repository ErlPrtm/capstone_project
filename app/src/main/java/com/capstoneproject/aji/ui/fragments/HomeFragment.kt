package com.capstoneproject.aji.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.preferences.UserPreferences
import com.capstoneproject.aji.databinding.FragmentHomeBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.ui.main.PegawaiViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PegawaiViewModel
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        viewModel = ViewModelProvider(this).get(PegawaiViewModel::class.java)

        checkAuthenticationAndLoadData()
        setDateToday()

        return binding.root
    }

    private fun checkAuthenticationAndLoadData() {
        lifecycleScope.launch {
            userPreferences.getToken().collect { token ->
                if (!token.isNullOrEmpty()) {
                    val username = extractUsernameFromToken(token)
                    binding.tvUsername.text = username ?: "Pengguna"
                    viewModel.getAllPegawai("Bearer $token")
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

    private fun setDateToday() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val dateToday = dateFormat.format(calendar.time)
        binding.tvDate.text = dateToday
    }

    private fun extractUsernameFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                val json = JSONObject(payload)
                json.optString("username", "Pengguna")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
