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

class AlbumSongAdapter(private var albumSongList: List<Song>, private var onSongClicked: (Song) -> Unit) :
    RecyclerView.Adapter<AlbumSongAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album_song, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return albumSongList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = albumSongList[position]
        holder.populate(song)
    }

    inner class ViewHolder(itemView: View) : ItemHolder<Song>(itemView) {
        private val rootLayout: RelativeLayout = itemView.findViewById(R.id.item_album_song_card)
        private val songTitleText: TextView = itemView.findViewById(R.id.item_album_song_title)
        private val songDurationText: TextView = itemView.findViewById(R.id.item_album_song_duration)
        private val songTrackNoText: TextView = itemView.findViewById(R.id.item_album_song_track_no)

        override fun populate(item: Song?) {
            songTitleText.text = item!!.title
            songDurationText.text = item.getFormattedDuration()
            songTrackNoText.text = item.getTrackNo()
            rootLayout.setOnClickListener {
                logI("Album Song clicked: $item")
                onSongClicked(item)
            }
        }
    }
}