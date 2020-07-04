package com.zc.phonoplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.SongAdapter
import com.zc.phonoplayer.adapter.SortOrder
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.ui.components.IndexedRecyclerView
import com.zc.phonoplayer.util.SONG_LIST
import com.zc.phonoplayer.util.showMenuPopup

class SongFragment : Fragment() {
    private lateinit var songList: ArrayList<Song>
    private lateinit var callback: SongAdapter.SongCallback
    private lateinit var recyclerView: IndexedRecyclerView
    private lateinit var recyclerAdapter: SongAdapter
    private lateinit var emptyText: TextView
    private lateinit var sortButton: ImageButton

    companion object {
        fun newInstance(songList: ArrayList<Song>): SongFragment {
            val frag = SongFragment()
            val args = Bundle()
            args.putParcelableArrayList(SONG_LIST, songList)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songList = arguments?.getParcelableArrayList(SONG_LIST) ?: ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        sortButton = view.findViewById(R.id.sort_button)
        emptyText = view.findViewById(R.id.empty_songs_text)

        if (songList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerAdapter = SongAdapter(songList, callback)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        }
        sortButton.setOnClickListener {
            requireContext().showMenuPopup(sortButton, R.menu.sort_song_menu, PopupMenu.OnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_sort_title_ascending -> recyclerAdapter.sortBy(SortOrder.ASCENDING)
                    R.id.action_sort_title_descending -> recyclerAdapter.sortBy(SortOrder.DESCENDING)
                    R.id.action_sort_by_artist -> recyclerAdapter.sortBy(SortOrder.ARTIST)
                    R.id.action_sort_by_album -> recyclerAdapter.sortBy(SortOrder.ALBUM)
                    R.id.action_sort_by_year -> recyclerAdapter.sortBy(SortOrder.YEAR)
                }
                true
            })
        }
        return view
    }

    fun filterData(query: String) {
        recyclerView.setIndexBarVisibility(false)
        recyclerAdapter.filterData(query)
    }

    fun setInitialData() {
        recyclerView.setIndexBarVisibility(true)
        recyclerAdapter.resetData()
        recyclerView.smoothScrollToPosition(0)
    }

    fun deleteSong(song: Song) {
        recyclerAdapter.deleteData(song)
    }

    fun setSongCallback(callback: SongAdapter.SongCallback) {
        this.callback = callback
    }
}
