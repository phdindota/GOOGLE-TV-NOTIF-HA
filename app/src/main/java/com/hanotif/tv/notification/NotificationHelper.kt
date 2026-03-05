package com.hanotif.tv.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import coil.load
import com.hanotif.tv.MainActivity
import com.hanotif.tv.R
import com.hanotif.tv.util.Constants

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Home Assistant camera event notifications"
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSystemNotification(
        title: String,
        message: String,
        streamUrl: String? = null,
        snapshotUrl: String? = null,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_TITLE, title)
            putExtra(Constants.EXTRA_MESSAGE, message)
            streamUrl?.let { putExtra(Constants.EXTRA_STREAM_URL, it) }
            snapshotUrl?.let { putExtra(Constants.EXTRA_SNAPSHOT_URL, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showOverlay(
        title: String,
        message: String,
        snapshotUrl: String? = null,
        streamUrl: String? = null,
        onOpenStream: (() -> Unit)? = null
    ) {
        if (!canDrawOverlay()) return

        dismissOverlay()

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.overlay_notification, null)

        view.findViewById<TextView>(R.id.tvOverlayTitle).text = title
        view.findViewById<TextView>(R.id.tvOverlayMessage).text = message

        val imageView = view.findViewById<ImageView>(R.id.ivOverlaySnapshot)
        if (!snapshotUrl.isNullOrBlank()) {
            imageView.visibility = View.VISIBLE
            imageView.load(snapshotUrl)
        } else {
            imageView.visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.btnOverlayOpen).apply {
            visibility = if (streamUrl != null) View.VISIBLE else View.GONE
            setOnClickListener {
                dismissOverlay()
                onOpenStream?.invoke()
            }
        }

        view.findViewById<TextView>(R.id.btnOverlayDismiss).setOnClickListener {
            dismissOverlay()
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 32
            y = 32
        }

        windowManager.addView(view, params)
        overlayView = view

        handler.postDelayed({ dismissOverlay() }, Constants.NOTIFICATION_OVERLAY_DISMISS_MS)
    }

    fun dismissOverlay() {
        handler.removeCallbacksAndMessages(null)
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View may already be removed
            }
        }
        overlayView = null
    }

    private fun canDrawOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}
