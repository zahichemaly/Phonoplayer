package com.zc.phonoplayer.adapter

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

class AlbumAdapter(private var albumList: List<Album>, private var onAlbumClicked: (Album) -> Unit) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    private lateinit var view: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
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
            onAlbumClicked(album)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_album_card)
        var albumTitleText: TextView = itemView.findViewById(R.id.item_album_title)
        var albumArtistText: TextView = itemView.findViewById(R.id.item_album_artist)
        var albumArt: CircleImageView = itemView.findViewById(R.id.item_album_art)
    }
}
