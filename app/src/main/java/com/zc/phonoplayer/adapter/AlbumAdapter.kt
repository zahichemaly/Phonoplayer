package com.zc.phonoplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        album?.let { a ->
            holder.albumTitleText.text = a.title
            holder.albumArtistText.text = a.artist
            context.loadUri(album.getAlbumArtUri().toString(), holder.albumArt)
            holder.rootLayout.setOnClickListener {
                logI("Album clicked: $album")
                callback.onAlbumClicked(a)
            }
            holder.rootLayout.setOnCreateContextMenuListener { menu, v, _ ->
                val editMenu = menu.add(0, v.id, 0, context.getString(R.string.edit))
                val deleteMenu = menu.add(0, v.id, 1, context.getString(R.string.delete))
                deleteMenu.setOnMenuItemClickListener {
                    callback.onAlbumDelete(a)
                    true
                }
                editMenu.setOnMenuItemClickListener {
                    callback.onAlbumEdit(a)
                    true
                }
            }
        }
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_album_card)
        var albumTitleText: TextView = itemView.findViewById(R.id.item_album_title)
        var albumArtistText: TextView = itemView.findViewById(R.id.item_album_artist)
        var albumArt: CircleImageView = itemView.findViewById(R.id.item_album_art)
    }

    interface AlbumCallback {
        fun onAlbumClicked(album: Album)
        fun onAlbumDelete(album: Album)
        fun onAlbumEdit(album: Album)
    }
}
