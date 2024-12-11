package com.capstoneproject.aji.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        homeViewModel = HomeViewModel(RetrofitInstance.api, userPreferences)

        checkAuthenticationAndLoadData()
        fetchAttendanceLogs()
        observeAttendanceLogs()

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

    private fun fetchAttendanceLogs() {
        lifecycleScope.launch {
            userPreferences.getUserId()
                .collect { userId ->
                    if(userId != null) {
                        homeViewModel.fetchAttendanceLogs(userId)
                    } else {
                        Log.e("HomeFragment", "User id is null")
                    }
                }
        }
    }

    private fun observeAttendanceLogs() {
        homeViewModel.attendanceLog.observe(viewLifecycleOwner) { attendanceLogs ->
            val latestLog = attendanceLogs?.lastOrNull()

            if(latestLog != null) {
                // Absence Card
                val latestMonth = attendanceLogs.maxByOrNull { log ->
                    parseDate(log.tanggal)?.time ?: 0
                } ?.tanggal?.let { extractMonthYear(it) }

                val filteredLogs = attendanceLogs.filter{ log ->
                    extractMonthYear(log.tanggal) == latestMonth
                }

                val totalAbsent = filteredLogs.count { log -> log.status_login == "absent"}
                binding.tvDaysAbsent.text = totalAbsent.toString()

                val currentMonth = latestLog.tanggal.let { extractMonth(it) }
                binding.tvMonthAbsent.text = currentMonth

                // Total Attended Card
                val totalAttended = filteredLogs.count {
                    it.status_login.isNotEmpty() && it.status_logout.isNotEmpty()
                }

                binding.tvMonthTotalAttended.text = latestMonth ?: "-"
                binding.tvDaysTotalAttended.text  = totalAttended.toString()
            } else {
                binding.tvDaysAbsent.text = "N/A"
                binding.tvDaysTotalAttended.text = "N/A"
            }
        }
    }

    private fun extractMonth(dateString: String): String {
        return try {
            val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()).parse(dateString)
            date?.let {
                SimpleDateFormat("MMMM", Locale.getDefault()).format(it)
            } ?: "N/A"
        } catch (e: Exception) {
            Log.e("AccountFragment", "Error exctracting month : ${e.message}")
            "N/A"
        }
    }

    private fun extractMonthYear(dateString: String): String {
        return try {
            val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()).parse(dateString)
            date?.let {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(it)
            } ?: "-"
        } catch (e: Exception) {
            Log.e("AccountFragment", "Error extracting month year : ${e.message}")
            "-"
        }
    }

    private fun formatTime(timeString: String): String {
        return if (!timeString.isNullOrEmpty()) {
            try {
                val time = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()).parse(timeString)
                time?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                } ?: "-"
            } catch (e: Exception) {
                Log.e("AccountFragment", "Error formatting time ${e.message}")
                "-"
            }
        } else {
            "-"
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            Log.e("AccountFragment", "Error parsing date ${e.message}")
            null
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
