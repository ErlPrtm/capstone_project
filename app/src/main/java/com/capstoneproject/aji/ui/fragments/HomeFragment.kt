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
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.adapter.AttendanceAdapter
import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.databinding.FragmentHomeBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceHelper: UserPreferences
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var attendanceAdapter : AttendanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        preferenceHelper = UserPreferences(requireContext())
        homeViewModel = HomeViewModel(RetrofitInstance.api, preferenceHelper)
        attendanceAdapter = AttendanceAdapter()

        checkAuthenticationAndLoadData()
        setDateToday()
        setGreetings()
        startRealTimeGreetingUpdate()

        setupRecyclerView()
        observeAttendanceLogs()
        fetchAttendanceLogs()

        setupDataCard()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvAttendanceHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
        }
    }

    private fun observeAttendanceLogs() {
        homeViewModel.attendanceLog.observe(viewLifecycleOwner) { logs ->
            logs?.let {
                attendanceAdapter.setData(it)
            } ?: Log.e("HomeFragment", "Attendance logs are null")
        }
    }

    private fun fetchAttendanceLogs() {
        lifecycleScope.launch {
            preferenceHelper.getUserId()
                .collect { userId ->
                    if(userId != null) {
                        homeViewModel.fetchAttendanceLogs(userId)
                    } else {
                        Log.e("HomeFragment", "User id is null")
                    }
                }
        }
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

    @SuppressLint("SetTextI18n")
    private fun setupDataCard() {
        homeViewModel.attendanceLog.observe(viewLifecycleOwner) { attendanceLogs ->
            val latestLog = attendanceLogs?.lastOrNull()

            if(latestLog != null) {
            // Attendance Card
                binding.tvNotesAttendance.text = latestLog.status_login
                binding.tvNotesCheckout.text = latestLog.status_logout

            // Checkout Card
                binding.tvClockAttendance.text = formatTime(latestLog.login_time)
                binding.tvClockCheckout.text = formatTime(latestLog.logout_time ?: "N/A")

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
                binding.tvClockAttendance.text = "N/A"
                binding.tvClockCheckout.text = "N/A"
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
            Log.e("HomeFragment", "Error exctracting month : ${e.message}")
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
            Log.e("HomeFragment", "Error extracting month year : ${e.message}")
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
                Log.e("HomeFragment", "Error formatting time ${e.message}")
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
            Log.e("HomeFragment", "Error parsing date ${e.message}")
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
