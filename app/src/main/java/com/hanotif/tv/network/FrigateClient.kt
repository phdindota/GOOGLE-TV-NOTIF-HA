package com.hanotif.tv.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanotif.tv.model.FrigateEvent
import com.hanotif.tv.util.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class FrigateClient(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(Constants.HTTP_CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
        .readTimeout(Constants.HTTP_READ_TIMEOUT_S, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private fun buildRequest(path: String): Request {
        return Request.Builder()
            .url("${baseUrl.trimEnd('/')}$path")
            .get()
            .build()
    }

    fun getEvents(limit: Int = 20, camera: String? = null): List<FrigateEvent> {
        val path = buildString {
            append("/api/events?limit=$limit")
            if (camera != null) append("&camera=$camera")
        }
        return try {
            client.newCall(buildRequest(path)).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val type = object : TypeToken<List<FrigateEvent>>() {}.type
                gson.fromJson(body, type) ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getSnapshotUrl(eventId: String): String =
        "${baseUrl.trimEnd('/')}/api/events/$eventId/snapshot.jpg"

    fun getClipUrl(eventId: String): String =
        "${baseUrl.trimEnd('/')}/api/events/$eventId/clip.mp4"

    fun getCameraSnapshotUrl(camera: String): String =
        "${baseUrl.trimEnd('/')}/api/$camera/latest.jpg"

    fun getCameras(): List<String> {
        return try {
            client.newCall(buildRequest("/api/config")).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val map: Map<String, Any> = gson.fromJson(body, type) ?: return emptyList()
                @Suppress("UNCHECKED_CAST")
                (map["cameras"] as? Map<String, Any>)?.keys?.toList() ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
