package com.zc.phonoplayer.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumSongAdapter
import com.zc.phonoplayer.loader.SongLoader
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.ui.viewModels.MainViewModel
import com.zc.phonoplayer.util.SELECTED_ALBUM
import com.zc.phonoplayer.util.loadUri
import de.hdodenhof.circleimageview.CircleImageView

class AlbumDetailsFragment : Fragment() {
    private lateinit var recyclerAdapter: AlbumSongAdapter
    private lateinit var recyclerView: RecyclerView
    private var album: Album? = null
    private lateinit var albumSongs: ArrayList<Song>
    private val mainViewModel: MainViewModel by activityViewModels()

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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album_details, container, false)
        val scrollView: NestedScrollView = view.findViewById(R.id.scroll_view)
        val albumArt: CircleImageView = view.findViewById(R.id.album_art)
        val albumTitleTv: TextView = view.findViewById(R.id.album_title)
        val albumArtistTv: TextView = view.findViewById(R.id.album_artist)
        val albumNbOfTracksTv: TextView = view.findViewById(R.id.album_nb_of_tracks)
        val shuffleButton: ImageButton = view.findViewById(R.id.shuffle_button)
        recyclerView = view.findViewById(R.id.recycler_view)
        album?.let { it ->
            albumTitleTv.text = it.title
            albumArtistTv.text = it.artist
            albumNbOfTracksTv.text = "${it.getNbOfTracks()} | ${it.year}"
            val albumUri = it.getAlbumArtUri().toString()
            requireContext().loadUri(albumUri, albumArt)
//            Glide.with(this)
//                .load(albumUri)
//                .placeholder(R.drawable.ic_default_music)
//                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 8)))
//                .into(albumHeader)
        }
        shuffleButton.setOnClickListener {
            mainViewModel.updatePlaylist(albumSongs)
        }
        recyclerAdapter = AlbumSongAdapter(albumSongs) { song -> mainViewModel.updatePlaylist(song, albumSongs) }
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = recyclerAdapter
        return view
    }
}
