package com.msit.minorproject

import android.util.Log

object Utils {
    const val PICK_IMAGE_REQUEST = 101
    const val NUM_CLASSES = 5

    private fun FloatArray.indexOfMax(): Int {
        var maxIndex = 0
        for (i in 1 until size) {
            if (this[i] > this[maxIndex]) {
                maxIndex = i
            }
        }
        return maxIndex
    }

    fun getDiagnosis(result: FloatArray): String {
        // Assuming result has shape [5]
        if (result.size != NUM_CLASSES) {
            Log.e("Error", "Unexpected result shape: ${result.size}")
            return "Unknown"
        }

        // Find the index of the maximum value
        val maxIndex = result.indexOfMax()

        // Map the index to a diagnosis label
        return when (maxIndex) {
            0 -> "Healthy"
            1 -> "Mild DR"
            2 -> "Moderate DR"
            3 -> "Profiliate DR"
            4 -> "Severe DR"
            else -> "Unknown"
        }
    }
}