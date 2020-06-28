package com.zc.phonoplayer.ui.fragments

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
import com.zc.phonoplayer.model.Playlist
import com.zc.phonoplayer.util.PLAYLIST

class PlaylistFragment : Fragment() {
    private lateinit var callback: PlaylistAdapter.PlaylistCallback
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: PlaylistAdapter
    private lateinit var playlists: ArrayList<Playlist>

    companion object {
        fun newInstance(playlists: ArrayList<Playlist>): PlaylistFragment {
            val frag = PlaylistFragment()
            val args = Bundle()
            args.putParcelableArrayList(PLAYLIST, playlists)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlists = arguments?.getParcelableArrayList(PLAYLIST) ?: ArrayList()
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
            recyclerAdapter = PlaylistAdapter(playlists, callback)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        }
        return view
    }

    fun setPlaylistCallback(callback: PlaylistAdapter.PlaylistCallback) {
        this.callback = callback
    }
}
