package com.capstoneproject.aji.data.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.capstoneproject.aji.data.model.AttendanceLog
import com.capstoneproject.aji.databinding.AttendanceHistoryTableBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    private var attendanceLogs = listOf<AttendanceLog>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newLogs: List<AttendanceLog>) {
        attendanceLogs = newLogs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = AttendanceHistoryTableBinding .inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val attendanceHistory = attendanceLogs[position]

        holder.bind(attendanceHistory)
    }

    override fun getItemCount() = attendanceLogs.size

    class AttendanceViewHolder(private val binding: AttendanceHistoryTableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("EEE, \ndd MMM yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(attendanceLog: AttendanceLog) {
            binding.tvDate.text = formatDate(attendanceLog.tanggal)
            binding.tvAttendance.text = formatTime(attendanceLog.login_time)
            binding.tvCheckout.text = formatTime(attendanceLog.logout_time) ?: "N/A"
            binding.tvTotalHours.text = calculateTotalHours(attendanceLog.login_time, attendanceLog.logout_time)
        }

        private fun formatDate(dateString: String): String {
            return try {
                val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()).parse(dateString)
                date?.let {
                    dateFormat.format(it)
                } ?: dateString
            } catch (e: Exception) {
                Log.e("AttendanceViewHolder", "Error parsing date: ${e.message}")
                dateString
            }
        }

        private fun formatTime(timeString: String?): String? {
            return if (!timeString.isNullOrEmpty()) {
                try {
                    val time = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()).parse(timeString)
                    time?.let {
                        timeFormat.format(it)
                    }
                } catch (e: Exception) {
                    Log.e("AttendanceViewHolder", "Error parsing time: ${e.message}")
                    null
                }
            } else {
                null
            }
        }

        private fun calculateTotalHours(loginTime: String, logoutTime: String?): String {
            if(logoutTime.isNullOrEmpty()) return "-"

            return try {
                val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault())

                val login = format.parse(loginTime)
                val logout = format.parse(logoutTime)

                val diff = (logout?.time ?: 0) - (login?.time ?: 0)
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(hours)

                val correctedMinutes = if(minutes < 0) 0 else minutes

                "$hours jam $correctedMinutes menit"
            } catch (e: Exception) {
                Log.e("AttendanceViewHolder", "Error calculating total hours: ${e.message}")
                "-"
            }
        }
    }
}

