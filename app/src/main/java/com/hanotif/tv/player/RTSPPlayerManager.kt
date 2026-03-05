package com.hanotif.tv.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView
import com.hanotif.tv.util.Constants

class RTSPPlayerManager(private val context: Context) {

    private var player: ExoPlayer? = null
    private var currentUrl: String? = null
    private var reconnectAttempts = 0
    private val handler = Handler(Looper.getMainLooper())

    private val reconnectRunnable = Runnable {
        currentUrl?.let { url ->
            if (reconnectAttempts < Constants.RTSP_MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++
                playInternal(url)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            scheduleReconnect()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                reconnectAttempts = 0
            }
        }
    }

    fun play(url: String, playerView: PlayerView) {
        currentUrl = url
        reconnectAttempts = 0
        playInternal(url)
        player?.let { playerView.player = it }
    }

    private fun playInternal(url: String) {
        releasePlayer()
        player = ExoPlayer.Builder(context).build().also { exoPlayer ->
            val mediaSource = RtspMediaSource.Factory()
                .createMediaSource(MediaItem.fromUri(url))
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.addListener(playerListener)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
        }
    }

    fun attachView(playerView: PlayerView) {
        playerView.player = player
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.play()
    }

    fun release() {
        handler.removeCallbacks(reconnectRunnable)
        releasePlayer()
        currentUrl = null
    }

    private fun releasePlayer() {
        player?.removeListener(playerListener)
        player?.release()
        player = null
    }

    private fun scheduleReconnect() {
        handler.postDelayed(reconnectRunnable, Constants.RTSP_RECONNECT_DELAY_MS)
    }
}
