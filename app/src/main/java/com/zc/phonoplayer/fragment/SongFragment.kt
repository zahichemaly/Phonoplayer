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
import com.zc.phonoplayer.adapter.SongAdapter
import com.zc.phonoplayer.fragment.OnSongClickedListener
import com.zc.phonoplayer.loader.SongLoader
import com.zc.phonoplayer.model.Song

class SongFragment : Fragment() {
    private lateinit var callback: OnSongClickedListener
    private var songList: ArrayList<Song> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: SongAdapter
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songList = SongLoader.newInstance().getSongs(requireActivity().applicationContext.contentResolver)
        callback.onSongListReady(songList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.isVerticalScrollBarEnabled = true

        emptyText = view.findViewById(R.id.empty_songs_text)
        if (songList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerView.isNestedScrollingEnabled = true
            recyclerAdapter = SongAdapter(songList) { song -> callback.onSongClicked(song) }
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        }
        return view
    }

    fun setOnSongClickedListener(callback: OnSongClickedListener) {
        this.callback = callback
    }
}
