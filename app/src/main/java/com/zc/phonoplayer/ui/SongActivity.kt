package com.zc.phonoplayer.ui

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.util.*
import kotlinx.android.synthetic.main.activity_song.*

class SongActivity : AppCompatActivity() {
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var seekBarThread: Thread
    private var song: Song? = null
    private var isAlbumSong = false
    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(this@SongActivity, token)
                MediaControllerCompat.setMediaController(this@SongActivity, mediaController)
            }
            mediaController.registerCallback(controllerCallback)
            if (isAlbumSong) playSelectedSong()
            initializeSeekBar()
            setupController()
        }
    }
    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                play_pause_button.background = this@SongActivity.drawable(R.drawable.exo_controls_pause)
            }
            if (state.state == PlaybackStateCompat.STATE_PAUSED) {
                play_pause_button.background = this@SongActivity.drawable(R.drawable.exo_controls_play)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            song = SongHelper.getSongFromMetadata(metadata)
            updateSong()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        val componentName = ComponentName(this, MusicService::class.java)
        mediaBrowser = MediaBrowserCompat(this, componentName, connectionCallback, null)
        song = intent.getParcelableExtra(SELECTED_SONG)
        isAlbumSong = intent.getBooleanExtra(IS_ALBUM_SONG, false)
        updateSong()
        setupSeekBar()
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

    private fun playSelectedSong() {
        song?.run {
            val extras = Bundle()
            extras.putParcelable(SELECTED_SONG, song)
            mediaController.transportControls?.playFromUri(getUri(), extras)
        }
    }

    private fun setupSeekBar() {
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                song_elapsed_time.text = TimeFormatter.getSongDuration(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mediaController.transportControls.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val position = seekBar.progress
                mediaController.transportControls.seekTo(position.toLong())
                mediaController.transportControls.play()
                seekBar.progress = position
            }
        })
    }

    private fun updateSong() {
        song?.let {
            song_title.text = it.title
            song_artist.text = it.artist
            song_duration.text = TimeFormatter.getSongDuration(it.duration.toInt())
            loadUri(it.albumArtUri, song_art)
            seek_bar.max = it.duration.toInt()
        }
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

    private fun setupController() {
        if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            play_pause_button.background = drawable(R.drawable.exo_controls_pause)
        } else {
            play_pause_button.background = drawable(R.drawable.exo_controls_play)
        }
        play_pause_button.setOnClickListener {
            when (mediaController.playbackState.state) {
                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> {
                    mediaController.transportControls.play()
                    play_pause_button.background = drawable(R.drawable.exo_controls_pause)
                }
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> {
                    mediaController.transportControls.pause()
                    play_pause_button.background = drawable(R.drawable.exo_controls_play)
                }
            }
        }
    }
}
