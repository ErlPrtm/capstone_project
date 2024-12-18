package com.capstoneproject.aji.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.AttendanceLog
import com.capstoneproject.aji.data.model.SalaryData
import com.capstoneproject.aji.databinding.FragmentAnalyticsBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.viewmodel.AnalyticsViewModel
import com.capstoneproject.aji.viewmodel.HomeViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var analyticsViewModel: AnalyticsViewModel

    private val paymentParameter = ArrayList<BarEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        homeViewModel = HomeViewModel(RetrofitInstance.api, userPreferences)
        analyticsViewModel = AnalyticsViewModel(RetrofitInstance.api, userPreferences)

        checkAuthenticationAndLoadData()
        fetchAttendanceLogs()
        fetchPaymentParameter()
        observeAttendanceData()
        observePaymentParameter()

        return binding.root
    }
    private fun checkAuthenticationAndLoadData() {
        lifecycleScope.launch {
            val isLoggedIn = userPreferences.isLoggedIn().first()
            if (isLoggedIn) {
                val fullname = userPreferences.getUserDetail("fullname").first() ?: "Pengguna"
                binding.tvUsername.text = fullname
            } else {
                redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
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

    private fun observeAttendanceData() {
        homeViewModel.attendanceLog.observe(viewLifecycleOwner) { attendanceLogs ->
            if(!attendanceLogs.isNullOrEmpty()) {
//                Log.d("AnalyticsFragment", "Data listing, attendanceLogs $attendanceLogs")
                dataListing(attendanceLogs)
            } else {
                Log.e("AnalyticsFragment", "No Attendance Data Available")
            }
        }
    }

    private fun fetchPaymentParameter() {
        lifecycleScope.launch {
            val rawToken = userPreferences.getToken().firstOrNull()
            val token = if(rawToken.isNullOrBlank()) null else "Bearer $rawToken"

            if (token == null) {
                Log.e("AnalyticsFragment", "Token tidak ditemukan.")
                return@launch
            }

            analyticsViewModel.userPosition?.observe(viewLifecycleOwner) { userPosition ->
                Log.d("AnalyticsFragment", "User Position : $userPosition")
                val parameterId = if(userPosition == "Chef") 1 else 2
                Log.d("AnalyticsFragment", "Token: $token, Parameter ID: $parameterId")

                analyticsViewModel.fetchSalaryParameter("$token", parameterId)
            }

            analyticsViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val today = dateFormat.format(Date())
//        Log.d("AnalyticsFragment", "getToday date : $today")
        return today
    }

    private fun calculateCuts(filteredLogs: List<AttendanceLog>? = null, salaryData: SalaryData, status: String) : Double {
        val today = getTodayDate()

        val todayLog = filteredLogs?.filter {
            val logDate = try {
                val logDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsedDate = logDateFormat.format(SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(it.tanggal)!!)
//                Log.d("AnalyticsFragment", "Parsed Log Date from cuts: $parsedDate, Today: $today")
                parsedDate == today
            } catch (e: Exception) {
                false
            }

            logDate && it.status_login == status
        } ?: emptyList()

        val count = todayLog.size
        Log.d("AnalyticsFragment", "count : $count")

        return when (status) {
            "late" -> count * salaryData.telat
            "absent" -> count * salaryData.absen
            else -> 0.0
        }
    }

    private fun calculateOvertimePayment(filteredLogs: List<AttendanceLog>? = null, salaryData: SalaryData) : Double {
        val today = getTodayDate()
        val standardLogoutHour = 21
        val fullDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }

        val logDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }

        val totalOvertimeHours = filteredLogs?.sumOf { log ->
            try {
                val logDate = fullDateFormat.parse(log.tanggal)
                val parsedDate = logDateFormat.format(logDate!!)
                Log.d("AnalyticsFragment", "Parsed Log Date from overtime: $parsedDate, Today: $today")

                if(parsedDate == today && log.status_logout == "overtime") {
                    val logoutTime = log.logout_time?.let { fullDateFormat.parse(it) }

                    if(logoutTime != null) {
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")).apply { time = logoutTime }
                        val logoutHours = calendar.get(Calendar.HOUR_OF_DAY)
                        Log.d("AnalyticsFragment", "Logout hour: $logoutHours, Standard logout: $standardLogoutHour")

                        return@sumOf maxOf(0, logoutHours - standardLogoutHour)
                    }
                }
            } catch (e: Exception) {
                Log.e("AnalyticsFragment", "Error parsing log data for overtime", e)
            }
            return@sumOf 0
        } ?: 0

        Log.d("AnalyticsFragment", "Total Overtime Hours: $totalOvertimeHours")
        return totalOvertimeHours * salaryData.lembur
    }

    @SuppressLint("SetTextI18n")
    private fun observePaymentParameter() {
        analyticsViewModel.salaryParameters.observe(viewLifecycleOwner) { paymentParameter ->
            if(!paymentParameter.isNullOrEmpty()) {
//                Log.d("AnalyticsFragment", "Data listing payment parameter $paymentParameter")

                val salaryData = paymentParameter.first()

                homeViewModel.attendanceLog.observe(viewLifecycleOwner) { attendanceLogs ->
                    if(!attendanceLogs.isNullOrEmpty()) {
                        val lateCut = calculateCuts(filteredLogs = attendanceLogs, salaryData = salaryData, status = "late")
                        val absenCut = calculateCuts(filteredLogs = attendanceLogs, salaryData = salaryData, status = "absent")
                        val overtimeFee = calculateOvertimePayment(filteredLogs = attendanceLogs, salaryData = salaryData)

                        binding.tvFeeMoney.text = formatToCurrency(salaryData.insentif)

                        binding.tvLateMoney.text = formatToCurrency(lateCut)
                        binding.tvAbsentMoney.text = formatToCurrency(absenCut)
                        binding.tvOvertimeMoney.text = formatToCurrency(overtimeFee)

                        val accumulatedFee = calculateAccumulatedFee(
                            salaryData.insentif,
                            overtimeFee,
                            lateCut,
                            absenCut
                        )

                        Log.d("Analyticsfragment", "Accumulated Fee : $accumulatedFee")
                        binding.tvAccumulatedMoney.text = formatToCurrency(accumulatedFee)
                    } else {
                        Log.e("AnalyticsFragment", "No attendance logs available")
                    }
                }
            } else {
                Log.e("Analyticsfragment", "No salary parameters available")
            }
        }
    }

    private fun formatToCurrency(amount: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        numberFormat.minimumFractionDigits = 0
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }

    private fun calculateAccumulatedFee(
        insentif: Double,
        overtimeFee: Double,
        lateCut: Double,
        absentCut: Double
        ): Double {

        return (insentif + overtimeFee) - (lateCut + absentCut)
    }


    private fun dataListing(attendanceLogs: List<AttendanceLog>) {
        paymentParameter.clear()

        // Ambil bulan dan tahun terakhir dari log
        val latestMonth = attendanceLogs.maxByOrNull { log ->
            parseDate(log.tanggal)?.time ?: 0
        }?.tanggal?.let { extractMonthYear(it) }

        // Filter log berdasarkan bulan terakhir
        val filteredLogs = attendanceLogs.filter { log ->
            extractMonthYear(log.tanggal) == latestMonth
        }

        // Hitung jumlah status per kategori untuk bulan terakhir
        val totalLate = filteredLogs.count { it.status_login == "late" }
        val totalAbsent = filteredLogs.count { it.status_login == "absent" }
        val totalOntime = filteredLogs.count { it.status_login == "on time" }

        Log.d("AnalyticsFragment", "Latest month: $latestMonth")
        Log.d("AnalyticsFragment", "Total late: $totalLate, Total absent: $totalAbsent, Total on time: $totalOntime for $latestMonth")

        // Tambahkan ke paymentParameter
        paymentParameter.add(BarEntry(0f, totalLate.toFloat()))
        paymentParameter.add(BarEntry(1f, totalAbsent.toFloat()))
        paymentParameter.add(BarEntry(2f, totalOntime.toFloat()))

        setChart()
    }

    private fun extractMonthYear(dateString: String): String {
        return try {
            val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(dateString)
            date?.let {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(it)
            } ?: "-"
        } catch (e: Exception) {
            Log.e("AnalyticsFragment", "Error extracting month year : ${e.message}")
            "-"
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(dateString)
        } catch (e: Exception) {
            Log.e("AnalyticsFragment", "Error parsing date ${e.message}")
            null
        }
    }

    private fun setChart() {
        if(paymentParameter.isEmpty()) {
            Log.d("AnalyticsFragment", "No data available for the chart")
            binding.barChart.clear()
            binding.barChart.invalidate()
            return
        }

        binding.barChart.description.isEnabled = false
        binding.barChart.setMaxVisibleValueCount(30)
        binding.barChart.setPinchZoom(false)
        binding.barChart.setDrawBarShadow(false)
        binding.barChart.setDrawGridBackground(false)

        val xAxis = binding.barChart.xAxis

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayListOf("Late", "Absent", "On time"))

        binding.barChart.axisLeft.setDrawGridLines(false)
        binding.barChart.legend.isEnabled = false

        val barDataSetter: BarDataSet

        if(binding.barChart.data != null && binding.barChart.data.dataSetCount > 0) {
            barDataSetter = binding.barChart.data.getDataSetByIndex(0) as BarDataSet
            barDataSetter.values = paymentParameter
            binding.barChart.data.notifyDataChanged()
            binding.barChart.notifyDataSetChanged()
        } else {
            barDataSetter = BarDataSet(paymentParameter, "Data Set")
            barDataSetter.setColors(*ColorTemplate.VORDIPLOM_COLORS)
            barDataSetter.setDrawValues(true)

            val dataSet = ArrayList<IBarDataSet>()
            dataSet.add(barDataSetter)

            val data = BarData(dataSet)
            binding.barChart.data = data
            binding.barChart.setFitBars(true)
        }

        binding.barChart.invalidate()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}