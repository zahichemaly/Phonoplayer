package com.zc.phonoplayer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumAdapter
import com.zc.phonoplayer.listeners.OnAlbumClickedListener
import com.zc.phonoplayer.loader.AlbumLoader
import com.zc.phonoplayer.model.Album

class AlbumFragment : Fragment() {
    private lateinit var callback: OnAlbumClickedListener
    private var albumList: ArrayList<Album> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: AlbumAdapter
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumList = AlbumLoader.getAlbums(requireActivity().applicationContext.contentResolver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.isVerticalScrollBarEnabled = true

        emptyText = view.findViewById(R.id.empty_songs_text)
        if (albumList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerView.isNestedScrollingEnabled = true
            recyclerAdapter = AlbumAdapter(albumList) { album -> callback.onAlbumClicked(album) }
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        }
        return view
    }

    fun setOnAlbumClickedListener(callback: OnAlbumClickedListener) {
        this.callback = callback
    }

}
