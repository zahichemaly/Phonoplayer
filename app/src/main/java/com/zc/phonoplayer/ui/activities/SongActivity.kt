package com.zc.phonoplayer.ui.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.zc.phonoplayer.R
import com.zc.phonoplayer.loader.SongLoader
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.service.RepeatMode
import com.zc.phonoplayer.service.SkipStatus
import com.zc.phonoplayer.util.*
import kotlinx.android.synthetic.main.activity_song.*

class SongActivity : AppCompatActivity() {
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var seekBarThread: Thread
    private lateinit var song: Song
    private var isAlbumSong = false
    private var isShuffleEnabled = false
    private var repeatMode = RepeatMode.OFF.value
    private lateinit var storageUtil: StorageUtil
    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(this@SongActivity, token)
                MediaControllerCompat.setMediaController(this@SongActivity, mediaController)
            }
            mediaController.registerCallback(controllerCallback)
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
        storageUtil = StorageUtil(this)
        isShuffleEnabled = storageUtil.getSavedShuffle()
        repeatMode = storageUtil.getSavedRepeatMode()
        val componentName = ComponentName(this, MusicService::class.java)
        mediaBrowser = MediaBrowserCompat(this, componentName, connectionCallback, null)
        song = intent.getParcelableExtra(SELECTED_SONG)!!
        isAlbumSong = intent.getBooleanExtra(IS_ALBUM_SONG, false)
        setupControls()
        setupSeekBar()
        setupListeners()
        updateSong()
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
        val extras = Bundle()
        extras.putParcelable(SELECTED_SONG, song)
        val songList = SongLoader.getSongsFromAlbum(contentResolver, song.albumId)
        //TODO get saved playlist
        val playlist = SongHelper.getDefaultPlaylist(song, songList)
        extras.putParcelableArrayList(SONG_LIST, playlist)
        mediaController.transportControls?.playFromUri(song.getUri(), extras)
    }

    private fun setupControls() {
        if (isShuffleEnabled) shuffle_button.background = drawable(R.drawable.exo_controls_shuffle_on)
        else shuffle_button.background = drawable(R.drawable.exo_controls_shuffle_off)
        when (repeatMode) {
            RepeatMode.OFF.value -> repeat_button.background = drawable(R.drawable.exo_controls_repeat_off)
            RepeatMode.ONE.value -> repeat_button.background = drawable(R.drawable.exo_controls_repeat_one)
            RepeatMode.ALL.value -> repeat_button.background = drawable(R.drawable.exo_controls_repeat_all)
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
        song_title.text = song.title
        song_artist.text = song.artist
        song_duration.text = TimeFormatter.getSongDuration(song.duration.toInt())
        loadUri(song.albumArtUri, song_art)
        seek_bar.max = song.duration.toInt()
    }

    private fun initializeSeekBar() {
        seekBarThread = object : Thread() {
            override fun run() {
                val totalDuration = song.duration
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
                PlaybackStateCompat.STATE_NONE -> {
                    playSelectedSong()
                }
                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                    mediaController.transportControls.play()
                    play_pause_button.background = drawable(R.drawable.exo_controls_pause)
                }
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> {
                    mediaController.transportControls.pause()
                    play_pause_button.background = drawable(R.drawable.exo_controls_play)
                }
            }
        }
        previous_button.setOnClickListener { skip(SkipStatus.SKIPPED_PREVIOUS) }
        next_button.setOnClickListener { skip(SkipStatus.SKIPPED_NEXT) }
        shuffle_button.setOnClickListener {
            isShuffleEnabled = !isShuffleEnabled
            if (isShuffleEnabled) {
                mediaController.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
                shuffle_button.background = drawable(R.drawable.exo_controls_shuffle_on)
                //Snackbar.make(header_layout, getString(R.string.shuffle_mode_, "ON"), Snackbar.LENGTH_SHORT).show()
            } else {
                mediaController.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
                shuffle_button.background = drawable(R.drawable.exo_controls_shuffle_off)
            }
        }
        repeat_button.setOnClickListener {
            repeatMode++
            if (repeatMode > RepeatMode.ALL.value) repeatMode = 0
            when (repeatMode) {
                RepeatMode.OFF.value -> {
                    mediaController.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
                    repeat_button.background = drawable(R.drawable.exo_controls_repeat_off)
                }
                RepeatMode.ONE.value -> {
                    mediaController.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
                    repeat_button.background = drawable(R.drawable.exo_controls_repeat_one)
                }
                RepeatMode.ALL.value -> {
                    mediaController.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
                    repeat_button.background = drawable(R.drawable.exo_controls_repeat_all)
                }
            }
        }
    }

    private fun skip(skipStatus: SkipStatus) {
        if (skipStatus == SkipStatus.SKIPPED_PREVIOUS) mediaController.transportControls.skipToPrevious()
        else if (skipStatus == SkipStatus.SKIPPED_NEXT) mediaController.transportControls.skipToNext()
        play_pause_button.background = drawable(R.drawable.exo_controls_pause)
    }

    private fun setupListeners() {
        back_button.setOnClickListener {
            finish()
        }
        volume_button.setOnClickListener {
            val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        }
        menu_button.setOnClickListener {
            showMenuPopup(menu_button, R.menu.song_playing_menu,
                PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_album -> openAlbum()
                        R.id.action_artist -> openArtist()
                    }
                    true
                })
        }
    }

    private fun openAlbum() {
        val intent = Intent()
        intent.putExtra(ALBUM_ID, song.albumId)
        setResult(RESULT_ALBUM_ID, intent)
        finish()
    }

    private fun openArtist() {
        val intent = Intent()
        intent.putExtra(ARTIST_ID, song.artistId)
        setResult(RESULT_ARTIST_ID, intent)
        finish()
    }
}
