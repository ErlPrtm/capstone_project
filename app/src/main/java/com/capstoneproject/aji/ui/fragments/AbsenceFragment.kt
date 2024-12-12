package com.capstoneproject.aji.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import com.capstoneproject.aji.data.UserPreferences
import com.capstoneproject.aji.data.repository.AuthRepository
import com.capstoneproject.aji.databinding.FragmentAbsenceBinding
import com.capstoneproject.aji.viewmodel.attendance.AttendanceViewModel
import com.capstoneproject.aji.viewmodel.attendance.AttendanceViewModelFactory
import com.capstoneproject.aji.ui.camera.CameraActivity
import com.capstoneproject.aji.ui.camera.CameraActivity.Companion.CAMERAX_RESULT
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val TAG = "AbsenceFragment"
        private const val REQUIRED_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

        // Lokasi target default (contoh: Mie Gemes Porong)
//        private const val TARGET_LAT = -7.5408109
//        private const val TARGET_LNG = 112.6967925
//        private const val RADIUS_METERS = 50f

        private const val TARGET_LAT = 37.4220936
        private const val TARGET_LNG = -122.083922
        private const val RADIUS_METERS = 50f

        // Variabel untuk menyimpan lokasi pengguna
        private var CURRENT_LAT: Double? = null
        private var CURRENT_LNG: Double? = null
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Izin lokasi berhasil diberikan.")
                showToast("Izin lokasi berhasil diberikan!")
                getCurrentLocation()
            } else {
                Log.e(TAG, "Izin lokasi ditolak.")
                showToast("Izin lokasi ditolak. Aktifkan di pengaturan!")
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (!allPermissionGranted()) {
            Log.d(TAG, "Meminta izin lokasi dari pengguna.")
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        } else {
            Log.d(TAG, "Izin lokasi sudah diberikan.")
            getCurrentLocation()
        }

        binding.btnCamera.setOnClickListener {
            Log.d(TAG, "Tombol kamera diklik.")
            startCameraX()
        }

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "Tombol simpan absensi diklik.")
            saveAttendance()
        }

        binding.btnGallery.setOnClickListener {
            Log.d(TAG, "Tombol galeri diklik.")
            openGallery()
        }

        setDateToday()
        updateClock()

        return binding.root
    }

    private fun allPermissionGranted(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Cek izin lokasi: $granted")
        return granted
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (!allPermissionGranted()) {
            Log.e(TAG, "Izin lokasi belum diberikan.")
            return
        }

        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).build()

        fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    CURRENT_LAT = location.latitude
                    CURRENT_LNG = location.longitude
                    Log.d(TAG, "Lokasi saat ini diperbarui: LAT=$CURRENT_LAT, LNG=$CURRENT_LNG")
                    showToast("Lokasi saat ini: LAT=$CURRENT_LAT, LNG=$CURRENT_LNG")
                    fusedLocationClient.removeLocationUpdates(this)
                } else {
                    Log.e(TAG, "Gagal mendapatkan lokasi saat ini.")
                    showToast("Gagal mendapatkan lokasi. Cek GPS Anda!")
                }
            }
        }, requireActivity().mainLooper)
    }


    private fun setDateToday() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val dateToday = dateFormat.format(calendar.time)
        binding.tvDate.text = dateToday
        Log.d(TAG, "Tanggal hari ini: $dateToday")
    }

    private fun updateClock() {
        lifecycleScope.launch {
            while (true) {
                val currentTime = Calendar.getInstance().time
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(currentTime)
                binding.tvClock.text = formattedTime
                kotlinx.coroutines.delay(1000)
            }
        }
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
            if (imageUri != null) {
                currentImageUri = imageUri
                Log.d(TAG, "Gambar berhasil diambil: $currentImageUri")
                showImage()
            } else {
                Log.e(TAG, "Gagal mengambil gambar.")
                showToast("Gagal mengambil gambar.")
            }
        }
    }

    private fun openGallery() {
        launcherIntentGallery.launch("image/*")
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            currentImageUri = uri
            Log.d(TAG, "Gambar dari galeri: $currentImageUri")
            showImage()
        } else {
            Log.e(TAG, "Gagal memilih gambar dari galeri.")
            showToast("Gagal memilih gambar dari galeri.")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d(TAG, "Menampilkan gambar: $it")
            binding.ivSelfiePreview.setImageURI(it)
        }
    }

    private fun saveAttendance() {
        lifecycleScope.launch {
            try {
                val userId = userPreferences.getUserId().firstOrNull().toString()
                val token = userPreferences.getToken().firstOrNull()

                Log.d(TAG, "Token: $token, User ID: $userId")
                if (userId == null || token == null) {
                    Log.e(TAG, "User ID atau token tidak ditemukan.")
                    showToast("User ID atau token tidak ditemukan. Pastikan Anda sudah login.")
                    return@launch
                }

                if (currentImageUri == null) {
                    Log.e(TAG, "Belum foto. URI gambar null.")
                    showToast("Belum foto nih, buruan selfie dulu!")
                    return@launch
                }

                if (CURRENT_LAT == null || CURRENT_LNG == null) {
                    Log.e(TAG, "Lokasi belum terdeteksi. LAT=$CURRENT_LAT, LNG=$CURRENT_LNG")
                    showToast("Lokasi kamu belum terdeteksi. Pastikan GPS aktif!")
                    return@launch
                }

                if (!isUserInTargetLocation(
                        Location("").apply {
                            latitude = CURRENT_LAT!!
                            longitude = CURRENT_LNG!!
                        },
                        TARGET_LAT,
                        TARGET_LNG,
                        RADIUS_METERS
                    )
                ) {
                    Log.e(TAG, "Lokasi pengguna di luar target.")
                    showToast("Kamu nggak ada di lokasi yang ditentukan. Absensi ditolak!")
                    return@launch
                }

                val file = convertUriToFile(currentImageUri!!)
                if (!file.exists()) {
                    Log.e(TAG, "File gambar tidak ditemukan.")
                    showToast("File foto tidak ditemukan. Pastikan Anda sudah mengambil gambar.")
                    return@launch
                }

                attendanceViewModel.absen(
                    token = token,
                    userId = userId,
                    fotoFile = file,
                    onSuccess = { response ->
                        Log.d(TAG, "Absensi berhasil: ${response.message}")
                        showToast("Yeay! Absensi berhasil: ${response.message}")
                    },
                    onError = { error ->
                        Log.e(TAG, "Gagal absensi: $error")
                        showToast("Aduh! Absensi gagal: $error")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error absensi: ${e.message}", e)
                showToast("Ups! Ada error: ${e.message ?: "Tidak diketahui"}")
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun isUserInTargetLocation(
        userLocation: Location,
        targetLat: Double,
        targetLng: Double,
        radiusInMeters: Float
    ): Boolean {
        val targetLocation = Location("").apply {
            latitude = targetLat
            longitude = targetLng
        }

        val distance = userLocation.distanceTo(targetLocation)
        val formattedDistance = String.format("%.0f", distance)
        Log.d(TAG, "Jarak ke lokasi target: $formattedDistance meter.")
        return distance <= radiusInMeters
    }

    private fun convertUriToFile(uri: Uri): File {
        val contentResolver = requireContext().contentResolver
        val tempFile = File(requireContext().cacheDir, "temp_image.jpg")
        tempFile.outputStream().use { outputStream ->
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Log.d(TAG, "File gambar berhasil disimpan: ${tempFile.path}")
        return tempFile
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
