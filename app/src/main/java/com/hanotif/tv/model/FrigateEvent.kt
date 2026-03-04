package com.hanotif.tv.model

import com.google.gson.annotations.SerializedName

data class FrigateEvent(
    @SerializedName("id") val id: String,
    @SerializedName("camera") val camera: String,
    @SerializedName("label") val label: String,
    @SerializedName("top_score") val topScore: Float = 0f,
    @SerializedName("start_time") val startTime: Double = 0.0,
    @SerializedName("end_time") val endTime: Double? = null,
    @SerializedName("has_snapshot") val hasSnapshot: Boolean = false,
    @SerializedName("has_clip") val hasClip: Boolean = false,
    @SerializedName("zones") val zones: List<String> = emptyList()
) {
    val snapshotPath: String get() = "/api/events/$id/snapshot.jpg"
    val clipPath: String get() = "/api/events/$id/clip.mp4"
}
