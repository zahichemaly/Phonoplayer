package com.zc.phonoplayer

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.util.Log
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.util.SELECTED_SONG
import com.zc.phonoplayer.util.TimeFormatter
import com.zc.phonoplayer.util.loadUri
import kotlinx.android.synthetic.main.activity_song.*

class SongActivity : AppCompatActivity() {
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var seekBarThread: Thread
    private var song: Song? = null
    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(this@SongActivity, token)
                MediaControllerCompat.setMediaController(this@SongActivity, mediaController)
            }
            mediaController.registerCallback(controllerCallback)
            initializeSeekBar()
            Log.d("onConnected", "Controller Connected")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Log.d("onConnected", "Connection Failed")
        }
    }
    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            val currentPosition = state?.position ?: 0L
            seek_bar.progress = currentPosition.toInt()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        val componentName = ComponentName(this, MusicService::class.java)
        mediaBrowser = MediaBrowserCompat(this, componentName, connectionCallback, null)
        song = intent.getParcelableExtra(SELECTED_SONG)
        populateUi()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()
        mediaController.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }


    private fun populateUi() {
        song?.let {
            song_title.text = it.songTitle
            song_artist.text = it.songArtist
            song_duration.text = TimeFormatter.stringForTime(it.duration)
            loadUri(it.getAlbumArtUri().toString(), song_art)
            seek_bar.max = it.duration
        }
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                song_elapsed_time.text = TimeFormatter.stringForTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeSeekBar() {
        seekBarThread = object : Thread() {
            override fun run() {
                val totalDuration = song?.duration ?: 0
                var currentPosition = mediaController.playbackState.position.toInt()
                seek_bar.progress = currentPosition
                while (currentPosition < totalDuration) {
                    try {
                        sleep(1000)
                        currentPosition = mediaController.playbackState.position.toInt()
                        seek_bar.progress = currentPosition
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        seekBarThread.start()
    }
}
