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
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

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
            val token = userPreferences.getToken().first()
            if (!token.isNullOrEmpty()) {
                val fullname = userPreferences.getUserDetail("fullname").firstOrNull() ?: "Pengguna"
                val email = userPreferences.getUserDetail("email").firstOrNull() ?: "info@aji.com"
                val nip = userPreferences.getUserDetail("nip").firstOrNull() ?: "123456789"
                val profileImageUrl =
                    userPreferences.getUserDetail("profile_image").firstOrNull() ?: ""

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
    private fun fetchAttendanceLogs() {
        lifecycleScope.launch {
            val userId = userPreferences.getUserId().first()
            if (userId != null) {
                try {
                    homeViewModel.fetchAttendanceLogs(userId)
                } catch (e: Exception) {
                    Log.e("AccountFragment", "Error fetching attendance logs: ${e.message}")
                }
            } else {
                Log.e("AccountFragment", "User ID is null or empty.")
            }
        }
    }

    private fun observeAttendanceLogs() {
        homeViewModel.attendanceLog.observe(viewLifecycleOwner) { attendanceLogs ->
            if (!attendanceLogs.isNullOrEmpty()) {
                val latestLog = attendanceLogs.lastOrNull()

                if (latestLog != null) {
                    val latestMonth = attendanceLogs.maxByOrNull { log ->
                        parseDate(log.tanggal)?.time ?: 0
                    }?.tanggal?.let { extractMonthYear(it) }

                    val filteredLogs = attendanceLogs.filter { log ->
                        extractMonthYear(log.tanggal) == latestMonth
                    }

                    val totalAbsent = filteredLogs.count { log -> log.status_login == "absent" }
                    binding.tvDaysAbsent.text = totalAbsent.toString()

                    val currentMonth = latestLog.tanggal.let { extractMonth(it) }
                    binding.tvMonthAbsent.text = currentMonth

                    val totalAttended = filteredLogs.count {
                        !it.status_login.isNullOrEmpty() && !it.status_logout.isNullOrEmpty()
                    }

                    binding.tvMonthTotalAttended.text = latestMonth ?: "-"
                    binding.tvDaysTotalAttended.text = totalAttended.toString()
                }
            } else {
                Log.e("AccountFragment", "Attendance logs are empty or null.")
                binding.tvDaysAbsent.text = "N/A"
                binding.tvDaysTotalAttended.text = "N/A"
            }
        }
    }

    private fun extractMonth(dateString: String?): String {
        return try {
            if (!dateString.isNullOrEmpty()) {
                val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(dateString)
                date?.let {
                    SimpleDateFormat("MMMM", Locale.getDefault()).format(it)
                } ?: "N/A"
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            Log.e("AccountFragment", "Error extracting month: ${e.message}")
            "N/A"
        }
    }

    private fun extractMonthYear(dateString: String?): String {
        return try {
            if (!dateString.isNullOrEmpty()) {
                val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(dateString)
                date?.let {
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(it)
                } ?: "-"
            } else {
                "-"
            }
        } catch (e: Exception) {
            Log.e("AccountFragment", "Error extracting month year: ${e.message}")
            "-"
        }
    }

    private fun parseDate(dateString: String?): Date? {
        return try {
            if (!dateString.isNullOrEmpty()) {
                SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(dateString)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AccountFragment", "Error parsing date: ${e.message}")
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
