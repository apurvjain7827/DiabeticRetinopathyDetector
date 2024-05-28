package com.msit.minorproject.ui

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import com.msit.minorproject.ImageDatabase
import com.msit.minorproject.ImageHistory
import com.msit.minorproject.ImageProcessor
import com.msit.minorproject.R
import com.msit.minorproject.Utils
import com.msit.minorproject.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var modelFile: File? = null
    private lateinit var interpreter: Interpreter
    private val options = Interpreter.Options()
    private var selectedImage: Uri? = null
    private val imageProcessor = ImageProcessor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser

        Glide.with(this).load(user!!.photoUrl).into(binding.profileIcon);

        binding.selectImageButton.setOnClickListener {
            pickImage()
        }

        binding.analyzeButton.setOnClickListener {
            if (selectedImage == null) {
                Toast.makeText(this, "Please Select an Image", Toast.LENGTH_SHORT).show()
            } else {
                analyzeTheImage()
            }
        }

        analyze()

        binding.profileIcon.setOnClickListener { view ->
            // Show options menu
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_profile -> {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }

    private fun logout() {
        // Implement your logout logic, e.g., sign out from Firebase
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        // Redirect to the login activity
         // Optional: Finish the current activity to prevent going back
    }


    private fun analyze() {
        val remoteModel = FirebaseCustomRemoteModel.Builder("Diabetic-Retinopathy-Detector").build()
        val conditions = FirebaseModelDownloadConditions.Builder()
            .requireWifi()
            .build()

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Setting up model...")
        progressDialog.setCancelable(false)

        progressDialog.show()

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnSuccessListener {
                FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                    .addOnCompleteListener { task: Task<File> ->
                        modelFile = task.result
                        interpreter = Interpreter(modelFile!!, options)
                        progressDialog.dismissWithDelay(1000) // Dismiss after 1 second
                    }
            }
            .addOnFailureListener {
                Log.e("Firebase Model", "Failed to download the remote model")
                progressDialog.dismissWithDelay(1000) // Dismiss after 1 second
                loadLocalModel()
            }
    }

    // Extension function to dismiss the ProgressDialog after a delay
    private fun ProgressDialog.dismissWithDelay(delayMillis: Long) {
        val handler = Handler()
        handler.postDelayed({
            dismiss()
        }, delayMillis)
    }



    private fun loadLocalModel() {
        Log.i("Info", "Trying Local Model")
        try {
            val tfliteModel = FileUtil.loadMappedFile(this, "model.tflite")
            interpreter = Interpreter(tfliteModel, options)
        } catch (e: IOException) {
            Log.e("tflite Support", "Error reading model", e)
        }
    }

    private fun analyzeTheImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.show()

        // Initial message
        progressDialog.setMessage("Analyzing image...")

        val handler = Handler()
        handler.postDelayed({
            progressDialog.setMessage("Processing data...")
            handler.postDelayed({
                progressDialog.setMessage("Applying algorithms...")
                handler.postDelayed({
                    progressDialog.setMessage("Generating result...")
                    handler.postDelayed({
                        progressDialog.dismiss()

                        // Perform the remaining tasks using coroutines
                        // Preprocess the image using ImageProcessor
                        val bitmap = imageProcessor.processImage(this, selectedImage!!)
                        val resizedBitmap = Bitmap.createScaledBitmap(bitmap!!, 224, 224, true)

                        // Run inference
                        val result = classifyImage(resizedBitmap)

                        // Display the result
                        val diagnosis = Utils.getDiagnosis(result)
                        Toast.makeText(this, "Diagnosis: $diagnosis", Toast.LENGTH_SHORT).show()

                        // Insert into the database
                        val imagePath = selectedImage.toString() // Use the image path or any unique identifier
                        val finalResult = "Diagnosis: $diagnosis" // Update this based on your result
                        val timestamp = System.currentTimeMillis()

                        val imageHistory = ImageHistory(imagePath = imagePath, result = finalResult, timestamp = timestamp)
                        val database = ImageDatabase.getInstance(this)
                        val historyDao = database.imageHistoryDao()

                        // Perform database operation asynchronously using coroutines
                        GlobalScope.launch(Dispatchers.IO) {
                            historyDao.insert(imageHistory)
                        }

                    }, 1000) // Delay after "Generating result..."
                }, 1000) // Delay after "Applying algorithms..."
            }, 1000) // Delay after "Processing data..."
        }, 1500) // Initial delay after "Analyzing image..."
    }




    private fun classifyImage(bitmap: Bitmap): FloatArray {
        val inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(224 * 224)
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        for (pixelValue in pixels) {
            inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        // Reset the position of inputBuffer to the beginning before running inference
        inputBuffer.rewind()

        // Run inference
        val output = Array(1) { FloatArray(Utils.NUM_CLASSES) }
        interpreter.run(inputBuffer, output)

        // Flatten the output to a 1D array
        return output.flatMap { it.asIterable() }.toFloatArray()
    }


    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, Utils.PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Utils.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImage = data.data!!


            // Load the selected image into the ImageView
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            binding.imageView.setImageBitmap(bitmap)

            // Apply zoom and centering
            applyZoomAndCentering(bitmap)
        }
    }

    private fun applyZoomAndCentering(bitmap: Bitmap) {
        val matrix = Matrix()

        // Zoom to fill the width or height, as needed
        val widthScale = binding.imageView.width.toFloat() / bitmap.width
        val heightScale = binding.imageView.height.toFloat() / bitmap.height
        val scaleFactor = widthScale.coerceAtLeast(heightScale)
        matrix.setScale(scaleFactor, scaleFactor)

        // Center the image within the ImageView
        val translateX = (binding.imageView.width - bitmap.width * scaleFactor) / 2f
        val translateY = (binding.imageView.height - bitmap.height * scaleFactor) / 2f
        matrix.postTranslate(translateX, translateY)

        binding.imageView.imageMatrix = matrix
    }


}
