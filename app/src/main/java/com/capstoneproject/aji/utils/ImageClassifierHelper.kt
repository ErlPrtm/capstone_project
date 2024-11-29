package com.capstoneproject.aji.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

class ImageClassifierHelper (
    val context: Context,
    val classifierListener: ClassifierListener?
){
//    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        // SetupImageClassifier kalo diperluin nanti buat validasi absen (depend ml nya)
    }

    fun classifyImage(image: Bitmap) {
        classifierListener?.onResults("Processing Complete", image)
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(
            message: String,
            image: Bitmap
        )
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}