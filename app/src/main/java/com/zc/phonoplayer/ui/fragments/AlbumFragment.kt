package com.zc.phonoplayer.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumAdapter
import com.zc.phonoplayer.adapter.SortOrder
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.ui.components.IndexedRecyclerView
import com.zc.phonoplayer.ui.viewModels.AlbumFragmentViewModel
import com.zc.phonoplayer.util.ALBUM_LIST
import com.zc.phonoplayer.util.HIDE_MENU
import com.zc.phonoplayer.util.showConfirmDialog
import com.zc.phonoplayer.util.showMenuPopup

class AlbumFragment : Fragment(), AlbumAdapter.AlbumCallback {
    private lateinit var albumList: ArrayList<Album>
    private lateinit var recyclerView: IndexedRecyclerView
    private lateinit var recyclerAdapter: AlbumAdapter
    private lateinit var emptyText: TextView
    private lateinit var sortButton: ImageButton
    private lateinit var gridButton: ImageButton
    private var hideMenu: Boolean = false
    private val albumFragmentViewModel: AlbumFragmentViewModel by activityViewModels()

    companion object {
        fun newInstance(albumList: ArrayList<Album>, hideMenu: Boolean = false): AlbumFragment {
            val frag = AlbumFragment()
            val args = Bundle()
            args.putParcelableArrayList(ALBUM_LIST, albumList)
            args.putBoolean(HIDE_MENU, hideMenu)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumList = arguments?.getParcelableArrayList(ALBUM_LIST) ?: ArrayList()
        hideMenu = arguments?.getBoolean(HIDE_MENU, false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        emptyText = view.findViewById(R.id.empty_songs_text)
        recyclerView = view.findViewById(R.id.recycler_view)
        sortButton = view.findViewById(R.id.sort_button)
        gridButton = view.findViewById(R.id.grid_button)
        if (albumList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            setupAdapter(LayoutType.LIST)
        }
        if (hideMenu) {
            sortButton.visibility = View.GONE
        } else {
            sortButton.visibility = View.VISIBLE
            sortButton.setOnClickListener {
                requireContext().showMenuPopup(sortButton, R.menu.sort_album_menu, PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_sort_title_ascending -> recyclerAdapter.sortBy(SortOrder.ASCENDING)
                        R.id.action_sort_title_descending -> recyclerAdapter.sortBy(SortOrder.DESCENDING)
                        R.id.action_sort_by_artist -> recyclerAdapter.sortBy(SortOrder.ARTIST)
                        R.id.action_sort_by_nb_of_tracks -> recyclerAdapter.sortBy(SortOrder.NB_OF_TRACKS)
                    }
                    true
                })
            }
        }
        gridButton.setOnClickListener {
            requireContext().showMenuPopup(gridButton, R.menu.grid_menu, PopupMenu.OnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_list -> setupAdapter(LayoutType.LIST)
                    R.id.action_grid_2x2 -> setupAdapter(LayoutType.GRID_2_BY_2)
                    R.id.action_grid_3x3 -> setupAdapter(LayoutType.GRID_3_BY_3)
                }
                true
            })
        }
        return view
    }

    private fun setupAdapter(layoutType: LayoutType) {
        when (layoutType) {
            LayoutType.LIST -> {
                recyclerAdapter = AlbumAdapter(albumList, this)
                recyclerView.layoutManager = LinearLayoutManager(activity)
                recyclerView.adapter = recyclerAdapter
            }
            LayoutType.GRID_2_BY_2 -> {
                recyclerAdapter = AlbumAdapter(albumList, this, true)
                recyclerView.layoutManager = GridLayoutManager(activity, 2)
                recyclerView.adapter = recyclerAdapter
            }
            LayoutType.GRID_3_BY_3 -> {
                recyclerAdapter = AlbumAdapter(albumList, this, true)
                recyclerView.layoutManager = GridLayoutManager(activity, 3)
                recyclerView.adapter = recyclerAdapter
            }
        }
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

    override fun onAlbumClicked(album: Album) {
        albumFragmentViewModel.set(album)
    }

    override fun onAlbumDelete(album: Album) {
        requireActivity().showConfirmDialog(
            title = getString(R.string.delete_album),
            message = getString(R.string.confirm_delete_album, album.getNbOfTracks()),
            listener = DialogInterface.OnClickListener { dialog, which ->
                //TODO
            })
    }

    override fun onAlbumEdit(album: Album) {
        //TODO
    }

    enum class LayoutType {
        LIST,
        GRID_2_BY_2,
        GRID_3_BY_3,
    }
}
