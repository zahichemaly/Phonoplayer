package com.zc.phonoplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.PlaylistAdapter
import com.zc.phonoplayer.loader.PlaylistLoader
import com.zc.phonoplayer.model.Playlist

class PlaylistFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: PlaylistAdapter
    private lateinit var playlists: List<Playlist>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlists = PlaylistLoader.getPlaylists(requireActivity().applicationContext.contentResolver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        val emptyText: TextView = view.findViewById(R.id.empty_playlist_text)
        if (playlists.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerView.isNestedScrollingEnabled = true
            recyclerAdapter = PlaylistAdapter(playlists)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        }
        return view
    }
}
