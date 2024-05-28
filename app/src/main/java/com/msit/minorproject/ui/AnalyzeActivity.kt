package com.msit.minorproject.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.msit.minorproject.databinding.ActivityAnalyzeBinding

class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyzeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        val message = intent.getStringExtra("Analysis")

        binding.analysisResultTextView.text = message

    }
}