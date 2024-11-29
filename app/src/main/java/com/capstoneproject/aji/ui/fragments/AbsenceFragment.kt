package com.capstoneproject.aji.ui.fragments

import android.Manifest
import android.app.Activity
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
import com.capstoneproject.aji.databinding.FragmentAbsenceBinding
import com.capstoneproject.aji.ui.camera.CameraActivity
import com.capstoneproject.aji.ui.camera.CameraActivity.Companion.CAMERAX_RESULT

class AbsenceFragment : Fragment() {
    private var _binding: FragmentAbsenceBinding ?= null
    private val binding get() = _binding


    private var currentImageUri: Uri? =null

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAbsenceBinding.inflate(layoutInflater)

        if(!allPermissionGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding!!.btnCamera!!.setOnClickListener{ startCameraX() }

        return binding!!.root
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

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "ShowImage: $it")
            binding?.ivSelfiePreview?.setImageURI(it)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
