package com.zc.phonoplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.logI

class AlbumSongAdapter(private var albumSongList: List<Song>, private var onSongClicked: (Song) -> Unit) : RecyclerView.Adapter<AlbumSongAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album_song, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return albumSongList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = albumSongList[position]
        holder.songTitleText.text = song.title
        holder.songDurationText.text = song.getFormattedDuration()
        holder.songTrackNoText.text = song.getTrackNo()
        holder.rootLayout.setOnClickListener {
            logI("Album Song clicked: $song")
            onSongClicked(song)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_album_song_card)
        val songTitleText: TextView = itemView.findViewById(R.id.item_album_song_title)
        val songDurationText: TextView = itemView.findViewById(R.id.item_album_song_duration)
        val songTrackNoText: TextView = itemView.findViewById(R.id.item_album_song_track_no)
    }
}