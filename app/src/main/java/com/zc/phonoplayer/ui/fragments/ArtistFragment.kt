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
import com.zc.phonoplayer.adapter.ArtistAdapter
import com.zc.phonoplayer.adapter.SortOrder
import com.zc.phonoplayer.model.Artist
import com.zc.phonoplayer.ui.components.IndexedRecyclerView
import com.zc.phonoplayer.util.ARTIST_LIST
import com.zc.phonoplayer.util.showMenuPopup

class ArtistFragment : Fragment() {
    private lateinit var callback: ArtistAdapter.ArtistCallback
    private lateinit var recyclerView: IndexedRecyclerView
    private var recyclerAdapter: ArtistAdapter? = null
    private lateinit var artistList: List<Artist>
    private lateinit var sortButton: ImageButton

    companion object {
        fun newInstance(artistList: ArrayList<Artist>): ArtistFragment {
            val frag = ArtistFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARTIST_LIST, artistList)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artistList = arguments?.getParcelableArrayList(ARTIST_LIST) ?: arrayListOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_artist, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        sortButton = view.findViewById(R.id.sort_button)
        val emptyText: TextView = view.findViewById(R.id.empty_songs_text)
        if (artistList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerAdapter = ArtistAdapter(artistList, callback)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        }
        sortButton.setOnClickListener {
            requireActivity().showMenuPopup(sortButton, R.menu.sort_artist_menu,
                PopupMenu.OnMenuItemClickListener {
                    recyclerAdapter?.run {
                        when (it.itemId) {
                            R.id.action_sort_title_ascending -> sortBy(SortOrder.ASCENDING)
                            R.id.action_sort_title_descending -> sortBy(SortOrder.DESCENDING)
                            R.id.action_sort_by_nb_of_tracks -> sortBy(SortOrder.NB_OF_TRACKS)
                            R.id.action_sort_by_nb_of_albums -> sortBy(SortOrder.NB_OF_ALBUMS)
                        }
                    }
                    true
                })
        }
        return view
    }

    fun filterData(query: String) {
        recyclerView.setIndexBarVisibility(false)
        recyclerAdapter?.filterData(query)
    }

    fun setInitialData() {
        recyclerView.setIndexBarVisibility(true)
        recyclerAdapter?.resetData()
        recyclerView.smoothScrollToPosition(0)
    }

    fun setArtistCallback(callback: ArtistAdapter.ArtistCallback) {
        this.callback = callback
    }
}
