package com.zc.phonoplayer

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.exoplayer2.util.Log
import com.zc.phonoplayer.adapter.TabAdapter
import com.zc.phonoplayer.fragment.AlbumFragment
import com.zc.phonoplayer.fragment.OnSongClickedListener
import com.zc.phonoplayer.fragment.SongFragment
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.util.SELECTED_SONG
import com.zc.phonoplayer.util.SONG_LIST
import com.zc.phonoplayer.util.loadUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_layout.*


const val READ_PERMISSION_GRANT = 100

class MainActivity : AppCompatActivity(), OnSongClickedListener {
    private var prevMenuItem: MenuItem? = null
    private lateinit var mMediaBrowserCompat: MediaBrowserCompat
    private var selectedSong: Song? = null
    private var songList: ArrayList<Song>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
        initializePlayer()
    }

    private val connectionCallback: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                super.onConnected()
                mMediaBrowserCompat.sessionToken.also { token ->
                    val mediaController = MediaControllerCompat(this@MainActivity, token)
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
            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                controller_play_button.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.exo_controls_pause))
            }
            if (state.state == PlaybackStateCompat.STATE_PAUSED) {
                controller_play_button.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.exo_controls_play))
            }
        }
    }

    private fun populateUi() {
        val tabAdapter = TabAdapter(supportFragmentManager)
        tabAdapter.addFragment(SongFragment(), "Tracks")
        tabAdapter.addFragment(AlbumFragment(), "Albums")
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
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION_GRANT)
        } else {
            populateUi()
        }
    }

    private fun initializePlayer() {
        val componentName = ComponentName(this, MusicService::class.java)
        mMediaBrowserCompat = MediaBrowserCompat(this, componentName, connectionCallback, null)
    }

    private fun setupController() {
        val mediaController = MediaControllerCompat.getMediaController(this)
        controller_play_button.setOnClickListener {
            when (mediaController.playbackState.state) {
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.STATE_NONE -> {
                    playSelectedSong()
                }
                PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.STATE_CONNECTING -> {
                    mediaController.transportControls.pause()
                    controller_play_button.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.exo_controls_play)
                    )
                }
            }
        }
        mediaController.registerCallback(mControllerCallback)
    }

    private fun playSelectedSong() {
        val mediaController = MediaControllerCompat.getMediaController(this)
        selectedSong?.run {
            val extras = Bundle()
            extras.putParcelable(SELECTED_SONG, selectedSong)
            extras.putParcelableArrayList(SONG_LIST, songList)
            mediaController.transportControls.playFromUri(getUri(), extras)
        }
        controller_play_button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.exo_controls_pause))
    }

    override fun onStart() {
        super.onStart()
        mMediaBrowserCompat.connect()
    }

    override fun onStop() {
        super.onStop()
        val controllerCompat = MediaControllerCompat.getMediaController(this)
        controllerCompat?.unregisterCallback(mControllerCallback)
        mMediaBrowserCompat.disconnect()
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is SongFragment) {
            fragment.setOnSongClickedListener(this)
        }
    }

    override fun onSongClicked(song: Song) {
        Log.i("Clicked", "song clicked is: " + song.songTitle)
        selectedSong = song
        controller_track.text = selectedSong!!.songTitle
        loadUri(selectedSong!!.getAlbumArtUri().toString(), controller_album_art)
        playSelectedSong()
    }

    override fun onSongListReady(songList: ArrayList<Song>) {
        //TODO cache
        this.songList = songList
    }
}
