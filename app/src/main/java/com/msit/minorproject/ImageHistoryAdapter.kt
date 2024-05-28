package com.msit.minorproject

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.msit.minorproject.databinding.ItemImageHistoryBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.sql.DataSource

// ImageHistoryAdapter.kt
class ImageHistoryAdapter : ListAdapter<ImageHistory, ImageHistoryAdapter.ImageHistoryViewHolder>(DiffCallback()) {

    class ImageHistoryViewHolder(private val binding: ItemImageHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageHistory: ImageHistory) {
            binding.resultTextView.text = imageHistory.result
            binding.timestampTextView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(imageHistory.timestamp))

            // Load the image using Glide or any other image loading library
            Glide.with(itemView.context)
                .load(Uri.parse(imageHistory.imagePath))
                .into(binding.historyImageView)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHistoryViewHolder {
        val binding = ItemImageHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<ImageHistory>() {
        override fun areItemsTheSame(oldItem: ImageHistory, newItem: ImageHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageHistory, newItem: ImageHistory): Boolean {
            return oldItem == newItem
        }
    }
}
