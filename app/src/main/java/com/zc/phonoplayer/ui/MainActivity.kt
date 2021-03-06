package com.zc.phonoplayer.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.exoplayer2.util.Log
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.TabAdapter
import com.zc.phonoplayer.listeners.OnAlbumClickedListener
import com.zc.phonoplayer.listeners.OnSongClickedListener
import com.zc.phonoplayer.loader.PlaylistLoader
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.ui.fragment.AlbumFragment
import com.zc.phonoplayer.ui.fragment.SongFragment
import com.zc.phonoplayer.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_layout.*

const val READ_PERMISSION_GRANT = 100

class MainActivity : AppCompatActivity(), OnSongClickedListener, OnAlbumClickedListener {
    private var prevMenuItem: MenuItem? = null
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    private var song: Song? = null
    private var songList: ArrayList<Song>? = null
    private lateinit var storageUtil: StorageUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storageUtil = StorageUtil(this)
        checkPermissions()
        initializePlayer()
        //Testing
        val playlists = PlaylistLoader.getPlaylists(contentResolver)
        playlists.forEach {
            Log.i("Playlist", it.toString())
        }
    }

    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(this@MainActivity, token)
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }
            setupController()
            Log.d("onConnected", "Controller Connected")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Log.d("onConnected", "Connection Failed")
        }
    }

    private val mControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            updateControllerState(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            song = SongHelper.getSongFromMetadata(metadata)
            updateSongController()
        }
    }

    private fun populateUi() {
        val tabAdapter = TabAdapter(supportFragmentManager)
        tabAdapter.addFragment(SongFragment(), getString(R.string.tracks))
        tabAdapter.addFragment(AlbumFragment(), getString(R.string.albums))
        view_pager.adapter = tabAdapter
        navigation_bar.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_songs -> view_pager.currentItem = 0
                R.id.item_albums -> view_pager.currentItem = 1
                R.id.item_artists -> view_pager.currentItem = 2
                R.id.item_genres -> view_pager.currentItem = 3
                R.id.item_playlists -> {
                }
            }
            true
        }

        view_pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(position: Int) {
                prevMenuItem?.let {
                    it.isChecked = false
                }
                navigation_bar.menu.getItem(position).isChecked = true
                prevMenuItem = navigation_bar.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })

        controller_layout_view.visibility = View.GONE
        controller_layout_view.setOnClickListener {
            val intent = Intent(this, SongActivity::class.java)
            intent.putExtra(SELECTED_SONG, song)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READ_PERMISSION_GRANT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    populateUi()
                } else {
                    //TODO permission denied
                }
            }
            else -> {
            }
        }
    }

    private fun checkPermissions() {
        Log.i("Permission", "Checking permissions")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_PERMISSION_GRANT
            )
        } else {
            populateUi()
        }
    }

    private fun initializePlayer() {
        val componentName = ComponentName(this, MusicService::class.java)
        mediaBrowser = MediaBrowserCompat(this, componentName, connectionCallback, null)
    }

    private fun updateControllerState(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
            controller_play_button.setImageDrawable(drawable(R.drawable.exo_controls_pause))
        }
        if (state.state == PlaybackStateCompat.STATE_PAUSED) {
            controller_play_button.setImageDrawable(drawable(R.drawable.exo_controls_play))
        }
    }

    private fun setupController() {
        controller_play_button.setOnClickListener {
            when (mediaController?.playbackState?.state) {
                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> {
                    playSelectedSong()
                }
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> {
                    mediaController?.transportControls?.pause()
                    controller_play_button.setImageDrawable(drawable(R.drawable.exo_controls_play))
                }
            }
        }
        mediaController?.registerCallback(mControllerCallback)
        val cachedSong = storageUtil.getStoredSong()
        if (cachedSong != null) {
            song = cachedSong
            updateSongController()
            mediaController?.transportControls?.seekTo(storageUtil.getStoredPosition())
        }
    }

    private fun playSelectedSong() {
        song?.run {
            val extras = Bundle()
            extras.putParcelable(SELECTED_SONG, song)
            extras.putParcelableArrayList(SONG_LIST, songList)
            mediaController?.transportControls?.playFromUri(getUri(), extras)
        }
    }

    private fun updateSongController() {
        if (controller_layout_view.visibility == View.GONE) controller_layout_view.visibility = View.VISIBLE
        controller_song_title.text = song!!.title
        controller_song_artist.text = song!!.artist
        loadUri(song?.albumArtUri, controller_song_art)
        mediaController?.playbackState?.let {
            updateControllerState(it)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!mediaBrowser.isConnected) mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        mediaController?.run {
            if (metadata != null) {
                song = SongHelper.getSongFromMetadata(metadata)
                updateSongController()
            }
        }
    }

    override fun onDestroy() {
        song?.let {
            storageUtil.storeSong(it)
        }
        storageUtil.storePosition(mediaController?.playbackState?.position ?: 0L)
        mediaController?.unregisterCallback(mControllerCallback)
        mediaBrowser.disconnect()
        super.onDestroy()
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is SongFragment) {
            fragment.setOnSongClickedListener(this)
        }
        if (fragment is AlbumFragment) {
            fragment.setOnAlbumClickedListener(this)
        }
    }

    override fun onSongClicked(song: Song) {
        this.song = song
        updateSongController()
        playSelectedSong()
    }

    override fun onSongListReady(songList: ArrayList<Song>) {
        //TODO cache
        this.songList = songList
    }

    override fun onAlbumClicked(album: Album) {
        val intent = Intent(this, AlbumActivity::class.java)
        intent.putExtra(SELECTED_ALBUM, album)
        startActivity(intent)
    }
}
