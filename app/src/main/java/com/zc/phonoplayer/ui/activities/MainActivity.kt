package com.zc.phonoplayer.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.TabAdapter
import com.zc.phonoplayer.loader.*
import com.zc.phonoplayer.model.*
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.ui.dialogs.EditAlbumDialogFragment
import com.zc.phonoplayer.ui.fragments.*
import com.zc.phonoplayer.ui.viewModels.MainViewModel
import com.zc.phonoplayer.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_layout.*

class MainActivity : BaseActivity() {
    private var prevMenuItem: MenuItem? = null
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    private var song: Song? = null
    private lateinit var songList: ArrayList<Song>
    private lateinit var storageUtil: StorageUtil
    private lateinit var searchView: SearchView
    private lateinit var tabAdapter: TabAdapter
    private lateinit var searchMenuItem: MenuItem
    private lateinit var mainViewModel: MainViewModel

    companion object {
        const val REQUEST_READ_PERMISSION = 1000
        const val REQUEST_CODE_SONG = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        storageUtil = StorageUtil(this)
        checkPermissions()
        initializePlayer()
        setupObservers()
    }

    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(this@MainActivity, token)
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }
            setupController()
            logD("Controller connected")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            logD("Controller connection failed")
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

    private fun setupObservers() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.playlist().observe(this, Observer { playlist ->
            this.songList = playlist.getSongList()
            this.song = playlist.getSelectedSong()
            if (playlist.isShuffled()) {
                val params = Bundle()
                params.putParcelableArrayList(SONG_LIST, songList)
                mediaController?.sendCommand(COMMAND_SHUFFLE_ALL, params, null)
            } else {
                updateSongController()
                playSelectedSong()
            }
        })
    }

    private fun populateUi() {
        songList = SongLoader.getSongs(contentResolver)
        val albums = AlbumLoader.getAlbums(contentResolver)
        val genres = GenreLoader.getGenreList(contentResolver)
        val artists = ArtistLoader.getArtistList(contentResolver)
        val playlists = PlaylistLoader.getPlaylists(contentResolver)

        tabAdapter = TabAdapter(supportFragmentManager)
        tabAdapter.addFragment(SongFragment.newInstance(songList), getString(R.string.tracks))
        tabAdapter.addFragment(AlbumFragment.newInstance(albums), getString(R.string.albums))
        tabAdapter.addFragment(ArtistFragment.newInstance(artists), getString(R.string.artists))
        tabAdapter.addFragment(GenreFragment.newInstance(genres), getString(R.string.genres))
        tabAdapter.addFragment(PlaylistFragment.newInstance(playlists), getString(R.string.playlists))

        view_pager.adapter = tabAdapter
        navigation_bar.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_songs -> view_pager.currentItem = 0
                R.id.item_albums -> view_pager.currentItem = 1
                R.id.item_artists -> view_pager.currentItem = 2
                R.id.item_genres -> view_pager.currentItem = 3
                R.id.item_playlists -> view_pager.currentItem = 4
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
            intent.putExtra(SONG_LIST, songList)
            startActivityForResult(intent, REQUEST_CODE_SONG)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_PERMISSION -> {
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
        logI("Checking permissions")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            logI("Permissions denied. Requesting...")
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_PERMISSION
            )
        } else {
            logI("Permissions granted!")
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
                    controller_play_button.setImageDrawable(drawable(R.drawable.exo_controls_pause))
                }
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> {
                    mediaController?.transportControls?.pause()
                    controller_play_button.setImageDrawable(drawable(R.drawable.exo_controls_play))
                }
            }
        }
        controller_previous_button.setOnClickListener {
            mediaController?.transportControls?.skipToPrevious()
            controller_play_button.setImageDrawable(drawable(R.drawable.exo_controls_play))
        }
        controller_next_button.setOnClickListener {
            mediaController?.transportControls?.skipToNext()
            controller_play_button.setImageDrawable(drawable(R.drawable.exo_controls_play))
        }
        mediaController?.registerCallback(mControllerCallback)
        val cachedSong = storageUtil.getSavedSong()
        if (cachedSong != null) {
            song = cachedSong
            updateSongController()
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
        mediaController?.sendCommand(COMMAND_DISCONNECT, null, null)
        mediaController?.unregisterCallback(mControllerCallback)
        mediaBrowser.disconnect()
        super.onDestroy()
    }

    override fun onAttachFragment(fragment: Fragment) {
        //TODO use view models
        when (fragment) {
            is ArtistFragment -> fragment.setArtistCallback(this)
            is AlbumFragment -> fragment.setAlbumCallback(this)
            is ArtistDetailsFragment -> fragment.setCallback(this)
            is GenreFragment -> fragment.setGenreCallback(this)
            is PlaylistFragment -> fragment.setPlaylistCallback(this)
        }
    }

    override fun onAlbumClicked(album: Album) {
        searchMenuItem.isVisible = false
        addFragment(R.id.frame_layout, AlbumDetailsFragment.newInstance(album), album.title)
    }

    override fun onAlbumDelete(album: Album) {
        showConfirmDialog(
            title = getString(R.string.delete_album),
            message = getString(R.string.confirm_delete_album, album.getNbOfTracks()),
            listener = DialogInterface.OnClickListener { dialog, which ->
                //TODO
            })
    }

    override fun onAlbumEdit(album: Album) {
        val dialog = EditAlbumDialogFragment.newInstance(album)
        dialog.show(supportFragmentManager, "edit dialog")
    }

    override fun onArtistClicked(artist: Artist) {
        val artistSongs = SongLoader.getArtistSongs(songList, artist.id)
        addFragment(R.id.frame_layout, ArtistDetailsFragment.newInstance(artist, artistSongs), artist.title)
    }

    override fun onGenreClicked(genre: Genre) {
        val genreSongs = SongLoader.getSongsFromGenre(contentResolver, genre.id)
        addFragment(R.id.frame_layout, SongFragment.newInstance(genreSongs), genre.name)
    }

    override fun onPlaylistClicked(playlist: Playlist) {
        val playlistSongs = playlist.songs ?: ArrayList()
        addFragment(R.id.frame_layout, SongFragment.newInstance(playlistSongs), playlist.name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        searchMenuItem = menu.findItem(R.id.action_search)

        searchView = searchMenuItem.actionView as SearchView

        val searchPlate: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        searchPlate.hint = getString(R.string.search_hint)
        searchPlate.setHintTextColor(color(R.color.light_grey))
        searchPlate.setTextColor(color(R.color.white))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                when (val currentFragment = tabAdapter.getCurrentFragment()) {
                    is SongFragment -> currentFragment.filterData(newText)
                    is AlbumFragment -> currentFragment.filterData(newText)
                    is ArtistFragment -> currentFragment.filterData(newText)
                }
                return true
            }
        })
        searchView.setOnCloseListener {
            when (val currentFragment = tabAdapter.getCurrentFragment()) {
                is SongFragment -> currentFragment.setInitialData()
                is AlbumFragment -> currentFragment.setInitialData()
                is ArtistFragment -> currentFragment.setInitialData()
            }
            searchView.onActionViewCollapsed()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SONG) {
            when (resultCode) {
                RESULT_ALBUM_ID -> {
                    val albumId = data?.getLongExtra(ALBUM_ID, 0L) ?: 0L
                    val album = AlbumLoader.getAlbumById(contentResolver, albumId)
                    if (album != null) {
                        addFragment(R.id.frame_layout, AlbumDetailsFragment.newInstance(album), album.title, true)
                    }
                }
                RESULT_ARTIST_ID -> {
                    val artistId = data?.getLongExtra(ARTIST_ID, 0L) ?: 0L
                    val artist = ArtistLoader.getArtistById(contentResolver, artistId)
                    val artistSongs = SongLoader.getArtistSongs(songList, artistId)
                    if (artist != null && artistSongs.isNotEmpty()) {
                        addFragment(R.id.frame_layout, ArtistDetailsFragment.newInstance(artist, artistSongs), artist.title, true)
                    }
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (!removeFragment(R.id.frame_layout)) {
            if (!searchView.isIconified) {
                searchView.isIconified = true
            } else {
                moveTaskToBack(true)
            }
        } else {
            searchMenuItem.isVisible = true
            setupActionBar(getString(R.string.app_name), false)
        }
    }
}
