package com.hanotif.tv.model

import com.google.gson.annotations.SerializedName

data class CameraStream(
    @SerializedName("name") val name: String,
    @SerializedName("rtsp_url") val rtspUrl: String,
    @SerializedName("snapshot_url") val snapshotUrl: String = ""
)
