package com.zc.phonoplayer.fragment

import `in`.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.AlbumAdapter
import com.zc.phonoplayer.loader.AlbumLoader
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.activity.AlbumActivity
import com.zc.phonoplayer.dialog.EditAlbumDialogFragment
import com.zc.phonoplayer.util.SELECTED_ALBUM
import com.zc.phonoplayer.util.showConfirmDialog

class AlbumFragment : Fragment(), AlbumAdapter.AlbumCallback {
    private var albumList: ArrayList<Album> = ArrayList()
    private lateinit var recyclerView: IndexFastScrollRecyclerView
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
            recyclerAdapter = AlbumAdapter(albumList, this)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
            recyclerView.setIndexBarCornerRadius(25)
            recyclerView.setIndexbarMargin(0f)
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
            message = getString(R.string.confirm_delete_album),
            listener = DialogInterface.OnClickListener { dialog, which ->
                //TODO delete album
            })
    }

    override fun onAlbumEdit(album: Album) {
        val dialog = EditAlbumDialogFragment.newInstance(album)
        dialog.show(parentFragmentManager, "edit dialog")
    }
}
