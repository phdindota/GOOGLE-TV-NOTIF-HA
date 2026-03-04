package com.hanotif.tv.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hanotif.tv.databinding.ItemEventBinding
import com.hanotif.tv.model.FrigateEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val onEventClick: (FrigateEvent) -> Unit
) : ListAdapter<FrigateEvent, EventAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FrigateEvent>() {
            override fun areItemsTheSame(oldItem: FrigateEvent, newItem: FrigateEvent) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: FrigateEvent, newItem: FrigateEvent) =
                oldItem == newItem
        }
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    inner class ViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: FrigateEvent) {
            binding.tvEventLabel.text = event.label.replaceFirstChar { it.uppercase() }
            binding.tvEventCamera.text = event.camera
            binding.tvEventTime.text = DATE_FORMAT.format(Date((event.startTime * 1000).toLong()))
            binding.tvEventScore.text = "${(event.topScore * 100).toInt()}%"

            binding.root.setOnClickListener { onEventClick(event) }
            binding.root.setOnFocusChangeListener { v, hasFocus ->
                v.alpha = if (hasFocus) 1.0f else 0.85f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
