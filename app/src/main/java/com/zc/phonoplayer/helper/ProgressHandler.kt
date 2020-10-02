package com.zc.phonoplayer.helper

import android.os.Handler
import android.os.Message
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import kotlin.math.max

class ProgressHandler(
    private val mediaController: MediaControllerCompat,
    private var totalDuration: Long = 0,
    private val callback: Callback,
    private var speed: Float = mediaController.playbackState.playbackSpeed,
    private var intervalPlaying: Int = (UPDATE_INTERVAL_PLAYING / speed).toInt(),
    private var intervalPaused: Int = (UPDATE_INTERVAL_PAUSED / speed).toInt(),
) : Handler() {

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            totalDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            speed = state.playbackSpeed
        }
    }

    companion object {
        private const val PROGRESS_UPDATE = 1
        private const val MIN_INTERVAL = 20
        private const val UPDATE_INTERVAL_PLAYING = 1000
        private const val UPDATE_INTERVAL_PAUSED = 500
    }

    fun start() {
        queueNextRefresh(1)
        mediaController.registerCallback(mediaControllerCallback)
    }

    fun stop() {
        removeMessages(PROGRESS_UPDATE)
        mediaController.unregisterCallback(mediaControllerCallback)
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (msg.what == PROGRESS_UPDATE) {
            queueNextRefresh(updateProgress().toLong())
        }
    }

    private fun updateProgress(): Int {
        val progressMillis = mediaController.playbackState.position.toInt()
        callback.onUpdateProgress(progressMillis)
        if (mediaController.playbackState.state != PlaybackStateCompat.STATE_PLAYING) {
            return intervalPaused
        }
        val remainingMillis = intervalPlaying - progressMillis % intervalPlaying
        return max(MIN_INTERVAL, remainingMillis)
    }


    private fun queueNextRefresh(delay: Long) {
        val message = obtainMessage(PROGRESS_UPDATE)
        removeMessages(PROGRESS_UPDATE)
        sendMessageDelayed(message, delay)
    }

    interface Callback {
        fun onUpdateProgress(progress: Int)
    }
}
