package com.hanotif.tv.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hanotif.tv.databinding.ItemCameraBinding
import com.hanotif.tv.model.CameraStream

class CameraAdapter(
    private val onCameraClick: (CameraStream) -> Unit
) : ListAdapter<CameraStream, CameraAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CameraStream>() {
            override fun areItemsTheSame(oldItem: CameraStream, newItem: CameraStream) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: CameraStream, newItem: CameraStream) =
                oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemCameraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(camera: CameraStream) {
            binding.tvCameraName.text = camera.name
            if (camera.snapshotUrl.isNotBlank()) {
                binding.ivCameraSnapshot.load(camera.snapshotUrl) {
                    crossfade(true)
                    error(android.R.drawable.ic_menu_camera)
                }
            }
            binding.root.setOnClickListener { onCameraClick(camera) }
            binding.root.setOnFocusChangeListener { v, hasFocus ->
                v.scaleX = if (hasFocus) 1.05f else 1.0f
                v.scaleY = if (hasFocus) 1.05f else 1.0f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCameraBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
