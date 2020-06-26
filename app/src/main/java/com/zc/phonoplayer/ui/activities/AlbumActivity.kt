package com.zc.phonoplayer.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumSongAdapter
import com.zc.phonoplayer.loader.SongLoader
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.IS_ALBUM_SONG
import com.zc.phonoplayer.util.SELECTED_ALBUM
import com.zc.phonoplayer.util.SELECTED_SONG
import com.zc.phonoplayer.util.loadUri
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_album.*

class AlbumActivity : AppCompatActivity() {
    private var album: Album? = null
    private lateinit var recyclerAdapter: AlbumSongAdapter
    private lateinit var albumSongList: List<Song>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        album = intent.getParcelableExtra(SELECTED_ALBUM)
        albumSongList = SongLoader.getSongsFromAlbum(contentResolver, album!!.id.toString())
        populateUi()
    }

    private fun populateUi() {
        album?.let {
            album_title.text = it.title
            album_artist.text = it.artist
            album_nb_of_tracks.text = it.getNbOfTracks()
            val albumUri = it.getAlbumArtUri().toString()
            loadUri(albumUri, album_art)
            Glide.with(this)
                .load(albumUri)
                .placeholder(R.drawable.ic_default_music)
                .apply(bitmapTransform(BlurTransformation(25, 8)))
                .into(album_header)
        }
        recyclerAdapter = AlbumSongAdapter(albumSongList) { song -> openSong(song) }
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerAdapter
    }

    private fun openSong(song: Song) {
        val intent = Intent(this, SongActivity::class.java)
        intent.putExtra(SELECTED_SONG, song)
        intent.putExtra(IS_ALBUM_SONG, true)
        startActivity(intent)
    }
}
