package com.hanotif.tv.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanotif.tv.util.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class HAClient(private val baseUrl: String, private val token: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(Constants.HTTP_CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
        .readTimeout(Constants.HTTP_READ_TIMEOUT_S, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private fun buildRequest(path: String): Request.Builder {
        return Request.Builder()
            .url("${baseUrl.trimEnd('/')}$path")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
    }

    fun getStates(): List<Map<String, Any>> {
        val request = buildRequest("/api/states").get().build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson(body, type) ?: emptyList()
        }
    }

    fun getEntityState(entityId: String): Map<String, Any>? {
        val request = buildRequest("/api/states/$entityId").get().build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(body, type)
        }
    }

    fun callService(domain: String, service: String, data: Map<String, Any>): Boolean {
        val json = gson.toJson(data)
        val body = json.toRequestBody("application/json".toMediaType())
        val request = buildRequest("/api/services/$domain/$service").post(body).build()
        return client.newCall(request).execute().use { it.isSuccessful }
    }

    fun getFcmToken(): String? {
        val request = buildRequest("/api/config").get().build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) null
            else response.body?.string()
        }
    }
}
