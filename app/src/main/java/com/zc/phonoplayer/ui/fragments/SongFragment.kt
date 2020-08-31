package com.zc.phonoplayer.ui.fragments

import android.app.Activity
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.SongAdapter
import com.zc.phonoplayer.adapter.SortOrder
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.ui.components.IndexedRecyclerView
import com.zc.phonoplayer.ui.dialogs.EditSongDialogFragment
import com.zc.phonoplayer.ui.viewModels.EditSongFragmentViewModel
import com.zc.phonoplayer.ui.viewModels.MainViewModel
import com.zc.phonoplayer.ui.viewModels.SongFragmentViewModel
import com.zc.phonoplayer.util.HIDE_MENU
import com.zc.phonoplayer.util.SONG_LIST
import com.zc.phonoplayer.util.showConfirmDialog
import com.zc.phonoplayer.util.showMenuPopup
import kotlinx.android.synthetic.main.fragment_song.*

class SongFragment : Fragment(), SongAdapter.SongCallback {
    private lateinit var songList: ArrayList<Song>
    private lateinit var recyclerView: IndexedRecyclerView
    private var recyclerAdapter: SongAdapter? = null
    private lateinit var emptyText: TextView
    private lateinit var sortButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private var hideMenu: Boolean = false
    private val mainViewModel: MainViewModel by activityViewModels()
    private val songViewModel: SongFragmentViewModel by activityViewModels()
    private val editSongViewModel: EditSongFragmentViewModel by activityViewModels()

    companion object {
        const val REQUEST_SONG_DELETE_PERMISSION = 1001

        fun newInstance(songList: ArrayList<Song>, hideMenu: Boolean = false): SongFragment {
            val frag = SongFragment()
            val args = Bundle()
            args.putParcelableArrayList(SONG_LIST, songList)
            args.putBoolean(HIDE_MENU, hideMenu)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songList = arguments?.getParcelableArrayList(SONG_LIST) ?: arrayListOf()
        hideMenu = arguments?.getBoolean(HIDE_MENU, false) ?: false
        songViewModel.item().observe(this, Observer { song ->
            recyclerAdapter?.selectSong(song)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        sortButton = view.findViewById(R.id.sort_button)
        shuffleButton = view.findViewById(R.id.shuffle_button)
        emptyText = view.findViewById(R.id.empty_songs_text)

        if (songList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerAdapter = SongAdapter(songList, this)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
            shuffleButton.setOnClickListener {
                mainViewModel.updatePlaylist(songList)
            }
            setupObservers()
        }
        if (hideMenu) {
            sortButton.visibility = View.GONE
        } else {
            sortButton.visibility = View.VISIBLE
        }
        sortButton.setOnClickListener {
            requireContext().showMenuPopup(sortButton, R.menu.sort_song_menu, PopupMenu.OnMenuItemClickListener {
                recyclerAdapter?.run {
                    when (it.itemId) {
                        R.id.action_sort_title_ascending -> sortBy(SortOrder.ASCENDING)
                        R.id.action_sort_title_descending -> sortBy(SortOrder.DESCENDING)
                        R.id.action_sort_by_artist -> sortBy(SortOrder.ARTIST)
                        R.id.action_sort_by_album -> sortBy(SortOrder.ALBUM)
                        R.id.action_sort_by_year -> sortBy(SortOrder.YEAR)
                    }
                }
                true
            })
        }
        return view
    }

    private fun setupObservers() {
        songViewModel.permissionToDelete().observe(viewLifecycleOwner, Observer { intentSender ->
            intentSender?.let {
                startIntentSenderForResult(intentSender, REQUEST_SONG_DELETE_PERMISSION, null, 0, 0, 0, null)
            }
        })
        songViewModel.nbOfDeletedSongs().observe(viewLifecycleOwner, Observer { nbOfDeletedSongs ->
            if (nbOfDeletedSongs > 0) {
                Snackbar.make(recycler_view, getString(R.string.nb_of_deleted_tracks, nbOfDeletedSongs), Snackbar.LENGTH_LONG)
                    .show()
                songViewModel.deletedSong?.let {
                    deleteSong(it)
                }
            }
        })
        editSongViewModel.nbOfUpdatedSongs().observe(viewLifecycleOwner, Observer { nbOfUpdatedSongs ->
            if (nbOfUpdatedSongs > 0) {
                Snackbar.make(recycler_view, getString(R.string.nb_of_updated_tracks, nbOfUpdatedSongs), Snackbar.LENGTH_LONG)
                    .show()
                editSongViewModel.updatedSong?.let {
                    updateData(it)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SONG_DELETE_PERMISSION && resultCode == Activity.RESULT_OK) {
            songViewModel.deletePendingSong()
        } else super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onSongClicked(song: Song) {
        mainViewModel.updatePlaylist(song, songList)
    }

    override fun onSongEdit(song: Song) {
        val dialog = EditSongDialogFragment.newInstance(song)
        dialog.show(requireActivity().supportFragmentManager, "edit dialog")
    }

    override fun onSongDeleted(song: Song) {
        requireContext().showConfirmDialog(
            title = getString(R.string.delete_track),
            message = getString(R.string.confirm_delete_song),
            listener = DialogInterface.OnClickListener { dialog, which ->
                songViewModel.deleteSong(song)
            })
    }

    fun filterData(query: String) {
        recyclerView.setIndexBarVisibility(false)
        recyclerAdapter?.filterData(query)
    }

    fun updateData(song: Song) {
        recyclerAdapter?.updateData(song)
    }

    fun setInitialData() {
        recyclerView.setIndexBarVisibility(true)
        recyclerAdapter?.resetData()
        recyclerView.smoothScrollToPosition(0)
    }

    fun deleteSong(song: Song) {
        recyclerAdapter?.deleteData(song)
    }
}
