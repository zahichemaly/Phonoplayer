package com.zc.phonoplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.util.loadUri
import com.zc.phonoplayer.util.logI
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class AlbumAdapter(private var albumList: ArrayList<Album>, private var callback: AlbumCallback, private var isGrid: Boolean = false) :
    IndexAdapter<AlbumAdapter.ViewHolder>(albumList.mapNotNull { a -> a.title }) {
    private lateinit var context: Context
    private lateinit var view: View
    private var filteredAlbumList = albumList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        view = if (isGrid) LayoutInflater.from(context).inflate(R.layout.item_album_grid, parent, false)
        else {
            LayoutInflater.from(context).inflate(R.layout.item_album, parent, false)
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredAlbumList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = filteredAlbumList.getOrNull(position)
        holder.populate(album)
    }

    fun filterData(query: String) {
        filteredAlbumList = albumList.filter { album ->
            album.title?.startsWith(query, ignoreCase = true) ?: false ||
                    album.artist?.startsWith(query, ignoreCase = true) ?: false
        }.toMutableList()
        notifyDataSetChanged()
    }

    fun sortBy(sortOrder: SortOrder) {
        when (sortOrder) {
            SortOrder.ASCENDING -> filteredAlbumList.sortBy { it.title?.toLowerCase(Locale.US) }
            SortOrder.DESCENDING -> filteredAlbumList.sortByDescending { it.title?.toLowerCase(Locale.US) }
            SortOrder.ARTIST -> filteredAlbumList.sortBy { it.artist?.toLowerCase(Locale.US) }
            SortOrder.NB_OF_TRACKS -> filteredAlbumList.sortByDescending { it.nbOfTracks }
            else -> filteredAlbumList
        }
        notifyDataSetChanged()
    }

    fun resetData() {
        this.filteredAlbumList = albumList.toMutableList()
        notifyDataSetChanged()
    }

    fun setInitialData() {
        this.filteredAlbumList = albumList
    }

    fun deleteData(album: Album) {
        val position = filteredAlbumList.indexOf(album)
        filteredAlbumList.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ViewHolder(itemView: View) : ItemHolder<Album>(itemView) {
        var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_album_card)
        var albumTitleText: TextView = itemView.findViewById(R.id.item_album_title)
        var albumArtistText: TextView = itemView.findViewById(R.id.item_album_artist)
        var albumArt: CircleImageView = itemView.findViewById(R.id.item_album_art)

        override fun populate(item: Album?) {
            item?.let { album ->
                albumTitleText.text = album.title
                albumArtistText.text = album.artist
                context.loadUri(item.getAlbumArtUri().toString(), albumArt)
                rootLayout.setOnClickListener {
                    logI("Album clicked: $album")
                    callback.onAlbumClicked(album)
                }
            }
        }
    }

    interface AlbumCallback {
        fun onAlbumClicked(album: Album)
        fun onAlbumDelete(album: Album)
        fun onAlbumEdit(album: Album)
    }
}
