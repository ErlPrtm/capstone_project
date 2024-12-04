package com.capstoneproject.aji.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.capstoneproject.aji.data.preferences.UserPreferences
import com.capstoneproject.aji.data.repository.AuthRepository
import com.capstoneproject.aji.databinding.FragmentAbsenceBinding
import com.capstoneproject.aji.ui.attendance.AttendanceViewModel
import com.capstoneproject.aji.ui.attendance.AttendanceViewModelFactory
import com.capstoneproject.aji.ui.camera.CameraActivity
import com.capstoneproject.aji.ui.camera.CameraActivity.Companion.CAMERAX_RESULT
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AbsenceFragment : Fragment() {
    private var _binding: FragmentAbsenceBinding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null
    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var userPreferences: UserPreferences

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if(isGranted) {
                showToast("permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private fun allPermissionGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if(uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            showToast("Gagal memilih gambar dari galery")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsenceBinding.inflate(inflater, container, false)

        val repository = AuthRepository()
        attendanceViewModel = ViewModelProvider(
            this,
            AttendanceViewModelFactory(repository)
        )[AttendanceViewModel::class.java]

        userPreferences = UserPreferences(requireContext())

        if(!allPermissionGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.btnCamera.setOnClickListener{ startCameraX() }
        binding.btnSave.setOnClickListener{ saveAttendance() }
        binding.btnGallery.setOnClickListener{ openGallery() }
        setDateToday()

        return binding.root
    }

    private fun setDateToday() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val dateToday = dateFormat.format(calendar.time)
        binding.tvDate.text = dateToday
    }

    private fun startCameraX() {
        val intent = Intent(requireContext(), CameraActivity::class.java)
        launcherIntentCamera.launch(intent)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == CAMERAX_RESULT) {
            val imageUri = result.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            if(imageUri != null) {
                currentImageUri = imageUri
                showImage()
            } else {
                showToast("Failed to capture image")
            }
        }
    }

    private fun openGallery() {
        launcherIntentGallery.launch("image/*")
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "ShowImage: $it")
            binding.ivSelfiePreview.setImageURI(it)
        }
    }

    private fun saveAttendance() {
        lifecycleScope.launch {
            try {
                val userId = userPreferences.getUserId().firstOrNull().toString()
                val token = userPreferences.getToken().firstOrNull()

                Log.d("SaveAttendance", "Save Dimulai untuk User Id: $userId, Token : $token")

                if (userId == null || token == null) {
                    showToast("User ID atau token tidak ditemukan")
                    Log.d("SaveAttendance", "Validasi gagal.\n UserId : $userId, \n Token: $token")
                    return@launch
                }

                if(currentImageUri == null) {
                    showToast("Foto belum ada broo, ambil dulu")
                    return@launch
                }

                val file = convertUriToFile(currentImageUri!!)
                if(!file.exists()) {
                    showToast("File foto tidak ditemukan")
                    return@launch
                }

                attendanceViewModel.absen(
                    token = token,
                    userId = userId,
                    fotoFile = file,
                    onSuccess = { response ->
                        showToast("Absensi berhasil: ${response.message}")
                        Log.d("saveAttendance", "${response.message}")
                    },
                    onError = { error ->
                        showToast("Absensi gagal euy: $error")
                        Log.d("saveAttendance", "Absen gagal karena $error")
                    }
                )
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }
    
    private fun convertUriToFile(uri: Uri) : File {
        val contentResolver = requireContext().contentResolver
        val tempFile = File(requireContext().cacheDir, "temp_image.jpg")
        tempFile.outputStream().use { outputStream ->
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
