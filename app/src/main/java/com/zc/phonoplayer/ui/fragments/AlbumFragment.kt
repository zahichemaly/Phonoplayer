package com.zc.phonoplayer.ui.fragments

import `in`.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumAdapter
import com.zc.phonoplayer.adapter.SortOrder
import com.zc.phonoplayer.loader.AlbumLoader
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.ui.activities.AlbumActivity
import com.zc.phonoplayer.ui.dialogs.EditAlbumDialogFragment
import com.zc.phonoplayer.util.SELECTED_ALBUM
import com.zc.phonoplayer.util.showConfirmDialog
import com.zc.phonoplayer.util.showMenuPopup

class AlbumFragment : Fragment(), AlbumAdapter.AlbumCallback {
    private var albumList: ArrayList<Album> = ArrayList()
    private lateinit var recyclerView: IndexFastScrollRecyclerView
    private lateinit var recyclerAdapter: AlbumAdapter
    private lateinit var emptyText: TextView
    private lateinit var sortButton: ImageButton
    private lateinit var gridButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumList = AlbumLoader.getAlbums(requireActivity().applicationContext.contentResolver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.isVerticalScrollBarEnabled = true
        sortButton = view.findViewById(R.id.sort_button)
        gridButton = view.findViewById(R.id.grid_button)

        emptyText = view.findViewById(R.id.empty_songs_text)
        if (albumList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerView.isNestedScrollingEnabled = true
            recyclerAdapter = AlbumAdapter(albumList, this)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            //recyclerView.layoutManager = GridLayoutManager(activity, 3)
            recyclerView.adapter = recyclerAdapter
            recyclerView.setIndexBarCornerRadius(25)
            recyclerView.setIndexbarMargin(0f)
        }

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

        gridButton.setOnClickListener {
            requireContext().showMenuPopup(gridButton, R.menu.grid_menu, PopupMenu.OnMenuItemClickListener {
                when(it.itemId) {
                    R.id.action_list -> {

                    }
                    R.id.action_grid_2x2 -> {

                    }
                    R.id.action_grid_3x3 -> {

                    }
                }
                true
            })
        }
        return view
    }

    override fun onAlbumClicked(album: Album) {
        val intent = Intent(context, AlbumActivity::class.java)
        intent.putExtra(SELECTED_ALBUM, album)
        startActivity(intent)
    }

    override fun onAlbumDelete(album: Album) {
        requireContext().showConfirmDialog(
            title = getString(R.string.delete_album),
            message = getString(R.string.confirm_delete_album, album.getNbOfTracks()),
            listener = DialogInterface.OnClickListener { dialog, which ->
                //TODO delete album
            })
    }

    override fun onAlbumEdit(album: Album) {
        val dialog = EditAlbumDialogFragment.newInstance(album)
        dialog.show(parentFragmentManager, "edit dialog")
    }
}
