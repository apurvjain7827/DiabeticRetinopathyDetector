package com.msit.minorproject.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.msit.minorproject.ImageDatabase
import com.msit.minorproject.ImageHistoryAdapter
import com.msit.minorproject.databinding.ActivityProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProfileBinding
    private lateinit var historyAdapter: ImageHistoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser

        setupProfile()
        setupHistoryRecyclerView()
        loadHistory()
    }

    private fun setupProfile() {
        // Set up your profile details (username, email, etc.) using binding
        // Example:
        val user = FirebaseAuth.getInstance().currentUser
        binding.usernameTextView.text = user?.displayName
        binding.emailTextView.text = user?.email

        // Load profile image using Glide (adjust as per your image loading library)
        Glide.with(this)
            .load(user?.photoUrl)
            .into(binding.profileImage)
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = ImageHistoryAdapter()
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun loadHistory() {
        val database = ImageDatabase.getInstance(this)
        val historyDao = database.imageHistoryDao()

        // Use Kotlin Coroutines to perform database operation in a background thread
        GlobalScope.launch(Dispatchers.IO) {
            val historyList = historyDao.getAll()

            // Update UI on the main thread
            withContext(Dispatchers.Main) {
                historyAdapter.submitList(historyList)
            }
        }
    }

}
