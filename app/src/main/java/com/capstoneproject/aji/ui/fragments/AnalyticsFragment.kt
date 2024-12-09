package com.capstoneproject.aji.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.api.RetrofitInstance
import com.capstoneproject.aji.data.model.AttendanceLog
import com.capstoneproject.aji.databinding.FragmentAnalyticsBinding
import com.capstoneproject.aji.ui.login.LoginActivity
import com.capstoneproject.aji.viewmodel.HomeViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Retrofit

class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var homeViewModel: HomeViewModel

    val paymentParameter = ArrayList<BarEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        homeViewModel = HomeViewModel(RetrofitInstance.api, userPreferences)

        checkAuthenticationAndLoadData()
        homeViewModel.attendanceLog.observe(viewLifecycleOwner) { attendanceLogs ->
            if(!attendanceLogs.isNullOrEmpty()) {
                dataListing(attendanceLogs)
                Log.d("AnalyticsFragment", "Data listing, attendanceLogs $attendanceLogs")
            } else {
                Log.e("AnalyticsFragment", "No Attendance Data Available")
            }
        }

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

    private fun dataListing(attendanceLogs: List<AttendanceLog>) {
        paymentParameter.clear()

        val totalLate = attendanceLogs.count { it.status_login == "late"}
        val totalAbsent = attendanceLogs.count { it.status_login == "absent"}
        val totalOntime = attendanceLogs.count { it.status_login == "on time"}

        Log.d("AnalyticsFragment", "Total late : $totalLate Total absent : $totalAbsent Total On time : $totalOntime")

        paymentParameter.add(BarEntry(0f, totalLate.toFloat()))
        paymentParameter.add(BarEntry(1f, totalAbsent.toFloat()))
        paymentParameter.add(BarEntry(2f, totalOntime.toFloat()))

        Log.d("AnalyticsFragment", "Payment parameter : $paymentParameter")

        setChart()
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