package com.zc.phonoplayer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumSongAdapter
import com.zc.phonoplayer.loader.SongLoader
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.ui.activities.SongActivity
import com.zc.phonoplayer.util.IS_ALBUM_SONG
import com.zc.phonoplayer.util.SELECTED_ALBUM
import com.zc.phonoplayer.util.SELECTED_SONG
import com.zc.phonoplayer.util.loadUri
import de.hdodenhof.circleimageview.CircleImageView

class AlbumDetailsFragment : Fragment() {
    private lateinit var recyclerAdapter: AlbumSongAdapter
    private lateinit var recyclerView: RecyclerView
    private var album: Album? = null
    private lateinit var albumSongs: ArrayList<Song>

    companion object {
        fun newInstance(album: Album): AlbumDetailsFragment {
            val frag = AlbumDetailsFragment()
            val args = Bundle()
            args.putParcelable(SELECTED_ALBUM, album)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        album = arguments?.getParcelable(SELECTED_ALBUM)
        albumSongs = SongLoader.getSongsFromAlbum(requireActivity().contentResolver, album!!.id)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album_details, container, false)
        val scrollView: NestedScrollView = view.findViewById(R.id.scroll_view)
        val albumArt: CircleImageView = view.findViewById(R.id.album_art)
        val albumTitleTv: TextView = view.findViewById(R.id.album_title)
        val albumArtistTv: TextView = view.findViewById(R.id.album_artist)
        val albumNbOfTracksTv: TextView = view.findViewById(R.id.album_nb_of_tracks)
        recyclerView = view.findViewById(R.id.recycler_view)
        album?.let {
            albumTitleTv.text = it.title
            albumArtistTv.text = it.artist
            albumNbOfTracksTv.text = it.getNbOfTracks()
            val albumUri = it.getAlbumArtUri().toString()
            requireContext().loadUri(albumUri, albumArt)
//            Glide.with(this)
//                .load(albumUri)
//                .placeholder(R.drawable.ic_default_music)
//                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 8)))
//                .into(albumHeader)
        }
        recyclerAdapter = AlbumSongAdapter(albumSongs) { song -> openSong(song) }
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = recyclerAdapter
        return view
    }

    private fun openSong(song: Song) {
        val intent = Intent(activity, SongActivity::class.java)
        intent.putExtra(SELECTED_SONG, song)
        intent.putExtra(IS_ALBUM_SONG, true)
        startActivity(intent)
    }
}