package com.zc.phonoplayer.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Album
import com.zc.phonoplayer.util.loadUri
import de.hdodenhof.circleimageview.CircleImageView

class AlbumAdapter(private var albumList: ArrayList<Album>, private var callback: AlbumCallback) :
    IndexAdapter<AlbumAdapter.ViewHolder>(albumList.mapNotNull { a -> a.title }) {
    private lateinit var context: Context
    private lateinit var view: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return albumList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albumList[position]
        holder.albumTitleText.text = album.title
        holder.albumArtistText.text = album.artist
        view.loadUri(album.getAlbumArtUri().toString(), holder.albumArt)
        holder.rootLayout.setOnClickListener {
            Log.i("AlbumAdapter", "Album Clicked: ${album.title}")
            callback.onAlbumClicked(album)
        }
        holder.rootLayout.setOnCreateContextMenuListener { menu, v, menuInfo ->
            val editMenu = menu.add(0, v.id, 0, context.getString(R.string.edit))
            val deleteMenu = menu.add(0, v.id, 1, context.getString(R.string.delete))
            deleteMenu.setOnMenuItemClickListener {
                callback.onAlbumDelete(album)
                true
            }
            editMenu.setOnMenuItemClickListener {
                callback.onAlbumEdit(album)
                true
            }
        }
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
