package com.hanotif.tv.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanotif.tv.model.CameraStream

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    var haUrl: String
        get() = prefs.getString(Constants.KEY_HA_URL, "") ?: ""
        set(value) = prefs.edit().putString(Constants.KEY_HA_URL, value).apply()

    var haToken: String
        get() = prefs.getString(Constants.KEY_HA_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(Constants.KEY_HA_TOKEN, value).apply()

    var frigateUrl: String
        get() = prefs.getString(Constants.KEY_FRIGATE_URL, "") ?: ""
        set(value) = prefs.edit().putString(Constants.KEY_FRIGATE_URL, value).apply()

    var cameras: List<CameraStream>
        get() {
            val json = prefs.getString(Constants.KEY_CAMERAS, null) ?: return emptyList()
            return try {
                val type = object : TypeToken<List<CameraStream>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            prefs.edit().putString(Constants.KEY_CAMERAS, gson.toJson(value)).apply()
        }

    fun isConfigured(): Boolean = haUrl.isNotBlank() || frigateUrl.isNotBlank()
}
