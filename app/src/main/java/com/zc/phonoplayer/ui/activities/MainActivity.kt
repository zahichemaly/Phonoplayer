package com.zc.phonoplayer.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.TabAdapter
import com.zc.phonoplayer.loader.*
import com.zc.phonoplayer.model.*
import com.zc.phonoplayer.service.MusicService
import com.zc.phonoplayer.ui.fragments.*
import com.zc.phonoplayer.ui.viewModels.*
import com.zc.phonoplayer.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_controller.*
import kotlinx.android.synthetic.main.layout_controller.view.*

class MainActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    private var song: Song? = null
    private lateinit var songList: ArrayList<Song>
    private lateinit var albumList: ArrayList<Album>
    private lateinit var artistList: ArrayList<Artist>
    private lateinit var genreList: ArrayList<Genre>
    private lateinit var playlistList: ArrayList<Playlist>
    private lateinit var preferenceUtil: PreferenceUtil
    private lateinit var sharedPreferencesUtil: SharedPreferencesUtil
    private lateinit var searchView: SearchView
    private lateinit var tabAdapter: TabAdapter
    private lateinit var prevMenuItem: MenuItem
    private lateinit var settingsMenuItem: MenuItem
    private lateinit var searchMenuItem: MenuItem
    private lateinit var mainViewModel: MainViewModel
    private lateinit var songFragmentViewModel: SongFragmentViewModel
    private lateinit var albumFragmentViewModel: AlbumFragmentViewModel
    private lateinit var artistFragmentViewModel: ArtistFragmentViewModel
    private lateinit var genreFragmentViewModel: GenreFragmentViewModel
    private lateinit var playlistFragmentViewModel: PlaylistFragmentViewModel

    companion object {
        const val REQUEST_READ_PERMISSION = 1000
        const val REQUEST_CODE_SONG = 1002
        const val REQUEST_CODE_SETTINGS = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferenceUtil = PreferenceUtil(this)
        song = preferenceUtil.getSavedSong()
        sharedPreferencesUtil = SharedPreferencesUtil(this, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        setSupportActionBar(toolbar)
        setSettings()
        setupObservers()
        checkPermissions()
        initializePlayer()
    }

    private fun setSettings() {
        when (sharedPreferencesUtil.getTheme()) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        val appName = sharedPreferencesUtil.getAppName()
        toolbar.title = appName
    }

    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(this@MainActivity, token)
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }
            logD("Controller connected")
            setupController()
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
            songFragmentViewModel.set(song!!)
        }
    }

    private fun setupObservers() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        songFragmentViewModel = ViewModelProvider(this).get(SongFragmentViewModel::class.java)
        albumFragmentViewModel = ViewModelProvider(this).get(AlbumFragmentViewModel::class.java)
        artistFragmentViewModel = ViewModelProvider(this).get(ArtistFragmentViewModel::class.java)
        genreFragmentViewModel = ViewModelProvider(this).get(GenreFragmentViewModel::class.java)
        playlistFragmentViewModel = ViewModelProvider(this).get(PlaylistFragmentViewModel::class.java)
        mainViewModel.playlist().observe(this, { playlist ->
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
        albumFragmentViewModel.item().observe(this, {
            searchMenuItem.isVisible = false
            addFragment(R.id.frame_layout, AlbumDetailsFragment.newInstance(it), it.title)
        })
        artistFragmentViewModel.item().observe(this, {
            val artistSongs = SongLoader.getArtistSongs(songList, it.id)
            addFragment(R.id.frame_layout, ArtistDetailsFragment.newInstance(it, artistSongs), it.title)
        })
        genreFragmentViewModel.item().observe(this, {
            val genreSongs = SongLoader.getSongsFromGenre(contentResolver, it.id)
            addFragment(R.id.frame_layout, SongFragment.newInstance(genreSongs), it.name)
        })
        playlistFragmentViewModel.item().observe(this, {
            val playlistSongs = it.songs ?: ArrayList()
            addFragment(R.id.frame_layout, SongFragment.newInstance(playlistSongs), it.name)
        })
    }

    private fun loadMedia() {
        //TODO cache
        songList = SongLoader.getSongs(applicationContext)
        albumList = AlbumLoader.getAlbums(contentResolver)
        artistList = ArtistLoader.getArtistList(contentResolver)
        genreList = GenreLoader.getGenreList(contentResolver)
        playlistList = PlaylistLoader.getPlaylists(contentResolver)
        populateUi()
    }

    private fun setupTabAdapter() {
        tabAdapter = TabAdapter(supportFragmentManager)
        val tabItems = sharedPreferencesUtil.getTabItems()
        tabItems.forEachIndexed { index, tabItem ->
            if (tabItem.isSelected) {
                when (tabItem.text) {
                    getString(R.string.tracks) -> {
                        navigation_bar.menu.add(0, R.id.item_tracks, index, tabItem.text).icon = drawable(R.drawable.ic_tracks)
                        tabAdapter.addFragment(SongFragment.newInstance(songList), tabItem.text)
                    }
                    getString(R.string.albums) -> {
                        navigation_bar.menu.add(0, R.id.item_albums, index, tabItem.text).icon = drawable(R.drawable.ic_album)
                        tabAdapter.addFragment(AlbumFragment.newInstance(albumList), tabItem.text)
                    }
                    getString(R.string.artists) -> {
                        navigation_bar.menu.add(0, R.id.item_artists, index, tabItem.text).icon = drawable(R.drawable.ic_artist)
                        tabAdapter.addFragment(ArtistFragment.newInstance(artistList), tabItem.text)
                    }
                    getString(R.string.genres) -> {
                        navigation_bar.menu.add(0, R.id.item_genres, index, tabItem.text).icon = drawable(R.drawable.ic_genre)
                        tabAdapter.addFragment(GenreFragment.newInstance(genreList), tabItem.text)
                    }
                    getString(R.string.playlists) -> {
                        navigation_bar.menu.add(0, R.id.item_playlists, index, tabItem.text).icon = drawable(R.drawable.ic_playlist)
                        tabAdapter.addFragment(PlaylistFragment.newInstance(playlistList), tabItem.text)
                    }
                }
            }
        }
        view_pager.adapter = tabAdapter
        // set first selected item
        navigation_bar.selectedItemId = navigation_bar.menu.getItem(0).itemId
        prevMenuItem = navigation_bar.menu.getItem(0)
        prevMenuItem.isChecked = true
        navigation_bar.setOnNavigationItemSelectedListener { menuItem ->
            view_pager.currentItem = menuItem.order
            true
        }
        view_pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(position: Int) {
                prevMenuItem.isChecked = false
                navigation_bar.menu.getItem(position).isChecked = true
                prevMenuItem = navigation_bar.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
    }

    private fun populateUi() {
        setupTabAdapter()
        controller_layout_view.controller_song_art.setOnClickListener {
            openSongDetails()
        }
        controller_layout_view.controller_song_layout.setOnClickListener {
            openSongDetails()
        }
    }

    private fun openSongDetails() {
        val intent = Intent(this, SongActivity::class.java)
        intent.putExtra(SELECTED_SONG, song)
        intent.putExtra(SONG_LIST, songList)
        startActivityForResult(intent, REQUEST_CODE_SONG)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadMedia()
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
            loadMedia()
        }
    }

    private fun initializePlayer() {
        val componentName = ComponentName(this, MusicService::class.java)
        mediaBrowser = MediaBrowserCompat(this, componentName, connectionCallback, null)
    }

    private fun updateControllerState(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
            controller_play_button.setImageDrawable(drawable(R.drawable.ic_controller_pause))
        }
        if (state.state == PlaybackStateCompat.STATE_PAUSED) {
            controller_play_button.setImageDrawable(drawable(R.drawable.ic_controller_play))
        }
    }

    private fun setupController() {
        controller_play_button.setOnClickListener {
            when (mediaController?.playbackState?.state) {
                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> {
                    mediaController?.transportControls?.play()
                    controller_play_button.setImageDrawable(drawable(R.drawable.ic_controller_pause))
                }
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> {
                    mediaController?.transportControls?.pause()
                    controller_play_button.setImageDrawable(drawable(R.drawable.ic_controller_play))
                }
            }
        }
        controller_previous_button.setOnClickListener {
            mediaController?.transportControls?.skipToPrevious()
            controller_play_button.setImageDrawable(drawable(R.drawable.ic_controller_play))
        }
        controller_next_button.setOnClickListener {
            mediaController?.transportControls?.skipToNext()
            controller_play_button.setImageDrawable(drawable(R.drawable.ic_controller_play))
        }
        mediaController?.registerCallback(mControllerCallback)
        updateSongController()
    }

    private fun playSelectedSong() {
        song?.let {
            val extras = Bundle()
            extras.putParcelable(SELECTED_SONG, it)
            extras.putParcelableArrayList(SONG_LIST, songList)
            mediaController?.transportControls?.playFromUri(it.getUri(), extras)
        }
    }

    private fun updateSongController() {
        logD("Updating controller view")
        controller_layout_view.isVisible = song != null
        song?.run {
            controller_song_title.isSelected = true
            controller_song_title.text = title
            controller_song_artist.text = artist
            loadUri(getAlbumArtUri().toString(), controller_song_art)
            mediaController?.playbackState?.let {
                updateControllerState(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!mediaBrowser.isConnected) mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(applicationContext).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(applicationContext).unregisterOnSharedPreferenceChangeListener(this)
        mediaController?.sendCommand(COMMAND_DISCONNECT, null, null)
        mediaController?.unregisterCallback(mControllerCallback)
        mediaBrowser.disconnect()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        searchMenuItem = menu.findItem(R.id.action_search)
        settingsMenuItem = menu.findItem(R.id.action_settings)

        settingsMenuItem.setOnMenuItemClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SETTINGS)
            true
        }

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

    @SuppressLint("RestrictedApi")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.pref_key_tab_settings_tab)) {
            navigation_bar.menu.clear()
            setupTabAdapter()
        }
        if (key == getString(R.string.pref_key_settings_play_speed)) {
            val playbackSpeed = sharedPreferencesUtil.getPlaybackSpeed()
            mediaController?.transportControls?.setPlaybackSpeed(playbackSpeed)
        }
    }
}
