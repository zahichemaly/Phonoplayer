package com.zc.phonoplayer.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.loadUri
import de.hdodenhof.circleimageview.CircleImageView

class SongAdapter(private var songList: List<Song>, private var onSongClicked: (Song) -> Unit) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    private lateinit var view: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songList[position]
        holder.songText.text = song.title
        holder.albumText.text = song.album
        holder.artistText.text = song.artist
        view.loadUri(song.getAlbumArtUri().toString(), holder.albumArt)
        holder.rootLayout.setOnClickListener {
            Log.i("SongAdapter", "Song clicked: " + song.title)
            onSongClicked(song)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_song_card)
        val songText: TextView = itemView.findViewById(R.id.item_track_text)
        val albumText: TextView = itemView.findViewById(R.id.item_album_text)
        val artistText: TextView = itemView.findViewById(R.id.item_artist_text)
        val albumArt: CircleImageView = itemView.findViewById(R.id.item_album_art)
    }
}