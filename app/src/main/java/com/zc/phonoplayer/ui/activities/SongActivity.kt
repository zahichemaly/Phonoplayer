package com.zc.phonoplayer.ui.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.zc.phonoplayer.R
import com.zc.phonoplayer.helper.ProgressHandler
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.service.RepeatMode
import com.zc.phonoplayer.service.SkipStatus
import com.zc.phonoplayer.ui.components.CircularSeekBar
import com.zc.phonoplayer.ui.fragments.SongQueueFragment
import com.zc.phonoplayer.util.*
import kotlinx.android.synthetic.main.activity_song.*
import kotlinx.android.synthetic.main.layout_now_playing.*


class SongActivity : AppCompatActivity(), ProgressHandler.Callback {
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var song: Song
    private var songList: ArrayList<Song>? = null
    private var isAlbumSong = false
    private var isShuffleEnabled = false
    private var repeatMode = RepeatMode.OFF.value
    private lateinit var preferenceUtil: PreferenceUtil
    private lateinit var progressHandler: ProgressHandler
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
                play_pause_button.background = this@SongActivity.drawable(R.drawable.ic_pause)
            }
            if (state.state == PlaybackStateCompat.STATE_PAUSED) {
                play_pause_button.background = this@SongActivity.drawable(R.drawable.ic_play)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            val songFromMetadata = SongHelper.getSongFromMetadata(metadata)
            if (songFromMetadata != song) {
                song = songFromMetadata
                updateSong()
                seek_bar.progress = 0
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        preferenceUtil = PreferenceUtil(this)
        isShuffleEnabled = preferenceUtil.getSavedShuffle()
        repeatMode = preferenceUtil.getSavedRepeatMode()
        val componentName = ComponentName(this, MusicService::class.java)
        mediaBrowser = MediaBrowserCompat(this, componentName, connectionCallback, null)
        song = intent.getParcelableExtra(SELECTED_SONG)!!
        songList = intent.getParcelableArrayListExtra(SONG_LIST) ?: arrayListOf()
        isAlbumSong = intent.getBooleanExtra(IS_ALBUM_SONG, false)
        setupControls()
        setupSeekBar()
        updateSong()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()
        progressHandler.stop()
        mediaController.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    private fun playSelectedSong() {
        val extras = Bundle()
        extras.putParcelable(SELECTED_SONG, song)
        extras.putParcelableArrayList(SONG_LIST, songList!!)
        mediaController.transportControls?.playFromUri(song.getUri(), extras)
    }

    private fun setupControls() {
        if (isShuffleEnabled) shuffle_button.background = drawable(R.drawable.ic_shuffle_enabled)
        else shuffle_button.background = drawable(R.drawable.ic_shuffle_disabled)
        when (repeatMode) {
            RepeatMode.OFF.value -> repeat_button.background = drawable(R.drawable.ic_repeat_disabled)
            RepeatMode.ONE.value -> repeat_button.background = drawable(R.drawable.ic_repeat_one)
            RepeatMode.ALL.value -> repeat_button.background = drawable(R.drawable.ic_repeat_all)
        }
    }

    private fun setupSeekBar() {
        seek_bar.setOnSeekBarChangeListener(object : CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(circularSeekBar: CircularSeekBar, progress: Int, fromUser: Boolean) {
                song_elapsed_time.text = TimeFormatter.getSongDuration(progress)
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {
                val position = seekBar.progress
                mediaController.transportControls.seekTo(position.toLong())
                mediaController.transportControls.play()
                seekBar.progress = position
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {
                mediaController.transportControls.pause()
            }
        })
    }

    private fun updateSong() {
        song_title.text = song.title
        song_artist.text = song.artist
        song_duration.text = TimeFormatter.getSongDuration(song.duration.toInt())
        loadUri(song.getAlbumArtUri().toString(), song_art)
        seek_bar.max = song.duration.toInt()
        setColors()
    }

    private fun setColors() {
        val bitmap = SongHelper.getBitmapFromUri(song.getAlbumArtUri(), contentResolver)
        val mutableBitmap = bitmap.copy(Bitmap.Config.RGB_565, true)
        val palette = SongHelper.createPaletteSync(mutableBitmap)
        seek_bar.circleColor = palette.getVibrantColor(color(R.color.sky_blue))
        seek_bar.circleProgressColor = palette.getLightVibrantColor(color(R.color.orange))
    }

    private fun initializeSeekBar() {
        progressHandler = ProgressHandler(mediaController, song.duration, this)
        progressHandler.start()
    }

    private fun setupController() {
        if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            play_pause_button.background = drawable(R.drawable.ic_pause)
        } else {
            play_pause_button.background = drawable(R.drawable.ic_play)
        }
        play_pause_button.setOnClickListener {
            when (mediaController.playbackState.state) {
                PlaybackStateCompat.STATE_NONE -> {
                    playSelectedSong()
                }
                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                    mediaController.transportControls.play()
                    play_pause_button.background = drawable(R.drawable.ic_pause)
                }
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> {
                    mediaController.transportControls.pause()
                    play_pause_button.background = drawable(R.drawable.ic_play)
                }
            }
        }
        previous_button.setOnClickListener { skip(SkipStatus.SKIPPED_PREVIOUS) }
        next_button.setOnClickListener { skip(SkipStatus.SKIPPED_NEXT) }
        shuffle_button.setOnClickListener {
            isShuffleEnabled = !isShuffleEnabled
            if (isShuffleEnabled) {
                mediaController.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
                shuffle_button.background = drawable(R.drawable.ic_shuffle_enabled)
                showSnackbar(getString(R.string.shuffle_mode_on))
            } else {
                mediaController.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
                shuffle_button.background = drawable(R.drawable.ic_shuffle_disabled)
                showSnackbar(getString(R.string.shuffle_mode_off))
            }
        }
        repeat_button.setOnClickListener {
            repeatMode++
            if (repeatMode > RepeatMode.ALL.value) repeatMode = 0
            when (repeatMode) {
                RepeatMode.OFF.value -> {
                    mediaController.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
                    repeat_button.background = drawable(R.drawable.ic_repeat_disabled)
                    showSnackbar(getString(R.string.repeat_off))
                }
                RepeatMode.ONE.value -> {
                    mediaController.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
                    repeat_button.background = drawable(R.drawable.ic_repeat_one)
                    showSnackbar(getString(R.string.repeat_one))
                }
                RepeatMode.ALL.value -> {
                    mediaController.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
                    repeat_button.background = drawable(R.drawable.ic_repeat_all)
                    showSnackbar(getString(R.string.repeat_all))
                }
            }
        }
        queue_button.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            var frag = supportFragmentManager.findFragmentByTag("queue_song_frag") as SongQueueFragment?
            if (frag == null) {
                frag = SongQueueFragment.newInstance()
                ft.add(R.id.frame_layout, frag, "queue_song_frag")
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            } else {
                ft.remove(frag)
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            }
            ft.commit()
        }
    }

    private fun skip(skipStatus: SkipStatus) {
        if (skipStatus == SkipStatus.SKIPPED_PREVIOUS) mediaController.transportControls.skipToPrevious()
        else if (skipStatus == SkipStatus.SKIPPED_NEXT) mediaController.transportControls.skipToNext()
        play_pause_button.background = drawable(R.drawable.ic_pause)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.song_playing_menu, menu)
        val volumeMenuItem = menu.findItem(R.id.action_volume)
        val detailsMenuItem = menu.findItem(R.id.action_details)
        val artistMenuItem = menu.findItem(R.id.action_artist)
        val albumMenuItem = menu.findItem(R.id.action_album)
        volumeMenuItem.setOnMenuItemClickListener {
            val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
            true
        }
        artistMenuItem.setOnMenuItemClickListener {
            val intent = Intent()
            intent.putExtra(ARTIST_ID, song.artistId)
            setResult(RESULT_ARTIST_ID, intent)
            finish()
            true
        }
        albumMenuItem.setOnMenuItemClickListener {
            val intent = Intent()
            intent.putExtra(ALBUM_ID, song.albumId)
            setResult(RESULT_ALBUM_ID, intent)
            finish()
            true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    override fun onUpdateProgress(progress: Int) {
        seek_bar.progress = progress
    }
}
