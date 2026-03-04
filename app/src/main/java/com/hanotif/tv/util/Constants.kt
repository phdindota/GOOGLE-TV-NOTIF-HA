package com.hanotif.tv.util

object Constants {
    // SharedPreferences keys
    const val PREFS_NAME = "hanotif_prefs"
    const val KEY_HA_URL = "ha_url"
    const val KEY_HA_TOKEN = "ha_token"
    const val KEY_FRIGATE_URL = "frigate_url"
    const val KEY_CAMERAS = "cameras_json"

    // FCM notification data keys
    const val FCM_KEY_CAMERA = "camera"
    const val FCM_KEY_EVENT_ID = "event_id"
    const val FCM_KEY_SNAPSHOT_URL = "snapshot_url"
    const val FCM_KEY_STREAM_URL = "stream_url"
    const val FCM_KEY_TITLE = "title"
    const val FCM_KEY_MESSAGE = "message"

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "hanotif_channel"
    const val NOTIFICATION_CHANNEL_NAME = "HA Notifications"
    const val NOTIFICATION_OVERLAY_DISMISS_MS = 15_000L

    // Fragment tags
    const val TAG_DASHBOARD = "dashboard"
    const val TAG_STREAM = "stream"
    const val TAG_SETTINGS = "settings"

    // Intent extras
    const val EXTRA_STREAM_URL = "extra_stream_url"
    const val EXTRA_CAMERA_NAME = "extra_camera_name"
    const val EXTRA_SNAPSHOT_URL = "extra_snapshot_url"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_MESSAGE = "extra_message"
    const val EXTRA_EVENT_ID = "extra_event_id"

    // Actions
    const val ACTION_SHOW_NOTIFICATION = "com.hanotif.tv.SHOW_NOTIFICATION"
    const val ACTION_OPEN_STREAM = "com.hanotif.tv.OPEN_STREAM"

    // Timeouts
    const val HTTP_CONNECT_TIMEOUT_S = 10L
    const val HTTP_READ_TIMEOUT_S = 30L

    // Player
    const val RTSP_RECONNECT_DELAY_MS = 3_000L
    const val RTSP_MAX_RECONNECT_ATTEMPTS = 5
}
