package com.capstoneproject.aji.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.databinding.FragmentHomeBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceHelper: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        preferenceHelper = UserPreferences(requireContext())

        checkAuthenticationAndLoadData()
        setDateToday()
        setGreetings()
        startRealTimeGreetingUpdate()

        return binding.root
    }

    private fun checkAuthenticationAndLoadData() {
        lifecycleScope.launch {
            val isLoggedIn = preferenceHelper.isLoggedIn().first()
            if (isLoggedIn) {
                val fullname = preferenceHelper.getUserDetail("fullname").first() ?: "Pengguna"
                binding.tvUsername.text = fullname
            } else {
                redirectToLogin()
            }
        }
    }

    private fun setGreetings() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            currentHour in 5..11 -> "Halo, Selamat Pagi"
            currentHour in 12..15 -> "Halo, Selamat Siang"
            currentHour in 16..18 -> "Halo, Selamat Sore"
            else -> "Halo, Selamat Malam"
        }

        binding.tvGreetings.text = greeting
    }

    private fun startRealTimeGreetingUpdate() {
        lifecycleScope.launch {
            while (true) {
                setGreetings()
                kotlinx.coroutines.delay(60 * 1000)
            }
        }
    }

    private fun setDateToday() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val dateToday = dateFormat.format(calendar.time)
        binding.tvDate.text = dateToday
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
