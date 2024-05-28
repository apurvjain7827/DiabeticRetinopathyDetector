package com.msit.minorproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log

class ImageProcessor {
    fun processImage(context: Context, imageUri: Uri?): Bitmap? {
        return try {
            // Read in an image file
            val originalBitmap = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(
                    imageUri!!
                )
            )

            // Resize the image to the desired size (IMG_SIZE x IMG_SIZE)
            var resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, IMG_SIZE, IMG_SIZE, true)

            // Convert the colour channel values from 0-255 to 0-1 values
            resizedBitmap = normalizeBitmap(resizedBitmap)
            resizedBitmap
        } catch (e: Exception) {
            Log.e("ImageProcessor", "Error processing image", e)
            null
        }
    }

    private fun normalizeBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Create a new Bitmap from normalized pixel values
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }



    companion object {
        private const val IMG_SIZE = 224 // Adjust according to your requirements
    }
}
