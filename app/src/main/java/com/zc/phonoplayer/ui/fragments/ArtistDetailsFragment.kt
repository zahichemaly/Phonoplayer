package com.zc.phonoplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumAdapter
import com.zc.phonoplayer.adapter.TabAdapter
import com.zc.phonoplayer.loader.AlbumLoader
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.model.Artist
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.ARTIST_SONG_LIST
import com.zc.phonoplayer.util.SELECTED_ARTIST

class ArtistDetailsFragment : Fragment() {
    private lateinit var albumCallback: AlbumAdapter.AlbumCallback
    private var artist: Artist? = null
    private var artistAlbums: ArrayList<Album> = ArrayList()
    private lateinit var artistSongs: ArrayList<Song>
    private lateinit var viewPager: ViewPager
    private lateinit var tabAdapter: TabAdapter
    private lateinit var tabLayout: TabLayout

    companion object {
        fun newInstance(artist: Artist, artistSongs: ArrayList<Song>): ArtistDetailsFragment {
            val frag = ArtistDetailsFragment()
            val args = Bundle()
            args.putParcelable(SELECTED_ARTIST, artist)
            args.putParcelableArrayList(ARTIST_SONG_LIST, artistSongs)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artist = arguments?.getParcelable(SELECTED_ARTIST)
        artistSongs = arguments?.getParcelableArrayList(ARTIST_SONG_LIST) ?: ArrayList()
        val uniqueAlbumIds = artistSongs.distinctBy { it.albumId }.map { it.albumId }
        uniqueAlbumIds.forEach { albumId ->
            val album = AlbumLoader.getAlbumById(requireActivity().contentResolver, albumId)
            if (album != null) artistAlbums.add(album)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_artist_details, container, false)
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)
        setupTabLayout()
        return view
    }

    private fun setupTabLayout() {
        tabLayout.setupWithViewPager(viewPager)
        tabAdapter = TabAdapter(childFragmentManager)
        tabAdapter.addFragment(SongFragment.newInstance(artistSongs, true), requireContext().getString(R.string.tracks_, artist!!.nbOfTracks))
        tabAdapter.addFragment(AlbumFragment.newInstance(artistAlbums, true), requireContext().getString(R.string.albums_, artist!!.nbOfAlbums))
        viewPager.adapter = tabAdapter
    }

    override fun onAttachFragment(childFragment: Fragment) {
        if (childFragment is AlbumFragment) childFragment.setAlbumCallback(albumCallback)
    }

    fun setCallback(albumCallback: AlbumAdapter.AlbumCallback) {
        this.albumCallback = albumCallback
    }
}
