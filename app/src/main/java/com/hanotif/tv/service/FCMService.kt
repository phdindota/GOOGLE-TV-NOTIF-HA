package com.hanotif.tv.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hanotif.tv.notification.NotificationHelper
import com.hanotif.tv.util.Constants
import com.hanotif.tv.util.PrefsManager

class FCMService : FirebaseMessagingService() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var prefs: PrefsManager

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        prefs = PrefsManager(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // FCM token is managed by HA Companion or custom automation setup.
        // Users can retrieve it from Firebase Console to configure HA notifications.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = data[Constants.FCM_KEY_TITLE]
            ?: notification?.title
            ?: "HA Notification"
        val message = data[Constants.FCM_KEY_MESSAGE]
            ?: notification?.body
            ?: ""
        val snapshotUrl = data[Constants.FCM_KEY_SNAPSHOT_URL]
        val streamUrl = data[Constants.FCM_KEY_STREAM_URL]
        val eventId = data[Constants.FCM_KEY_EVENT_ID]
        val camera = data[Constants.FCM_KEY_CAMERA]

        // Build snapshot URL from Frigate if event_id is present but snapshot_url is not
        val resolvedSnapshotUrl = snapshotUrl
            ?: if (eventId != null && prefs.frigateUrl.isNotBlank()) {
                "${prefs.frigateUrl.trimEnd('/')}/api/events/$eventId/snapshot.jpg"
            } else null

        // Show system notification (always)
        notificationHelper.showSystemNotification(
            title = title,
            message = message,
            streamUrl = streamUrl,
            snapshotUrl = resolvedSnapshotUrl
        )

        // Broadcast to show overlay in-app if app is running
        val broadcastIntent = Intent(Constants.ACTION_SHOW_NOTIFICATION).apply {
            setPackage(packageName)
            putExtra(Constants.EXTRA_TITLE, title)
            putExtra(Constants.EXTRA_MESSAGE, message)
            resolvedSnapshotUrl?.let { putExtra(Constants.EXTRA_SNAPSHOT_URL, it) }
            streamUrl?.let { putExtra(Constants.EXTRA_STREAM_URL, it) }
            eventId?.let { putExtra(Constants.EXTRA_EVENT_ID, it) }
            camera?.let { putExtra(Constants.EXTRA_CAMERA_NAME, it) }
        }
        sendBroadcast(broadcastIntent)
    }
}
