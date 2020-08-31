package com.zc.phonoplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Song
import com.zc.phonoplayer.util.color
import com.zc.phonoplayer.util.loadUri
import com.zc.phonoplayer.util.logI
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SongAdapter(private var songList: List<Song>, private var callback: SongCallback) :
    IndexAdapter<SongAdapter.ViewHolder>(songList.mapNotNull { it.title }) {
    private lateinit var view: View
    private lateinit var context: Context
    private var previousPosition: Int = -1
    private var filteredSongList = songList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredSongList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = filteredSongList.getOrNull(position)
        holder.populate(song)
    }

    fun filterData(query: String) {
        filteredSongList = songList.filter { song ->
            song.title?.startsWith(query, ignoreCase = true) ?: false ||
                    song.artist?.startsWith(query, ignoreCase = true) ?: false ||
                    song.album?.startsWith(query, ignoreCase = true) ?: false
        }.toMutableList()
        notifyDataSetChanged()
    }

    fun sortBy(sortOrder: SortOrder) {
        when (sortOrder) {
            SortOrder.ASCENDING -> filteredSongList.sortBy { it.title?.toLowerCase(Locale.US) }
            SortOrder.DESCENDING -> filteredSongList.sortByDescending { it.title?.toLowerCase(Locale.US) }
            SortOrder.ARTIST -> filteredSongList.sortBy { it.artist?.toLowerCase(Locale.US) }
            SortOrder.ALBUM -> filteredSongList.sortBy { it.album?.toLowerCase(Locale.US) }
            SortOrder.YEAR -> filteredSongList.sortBy { it.year }
            else -> filteredSongList
        }
        notifyDataSetChanged()
    }

    fun resetData() {
        this.filteredSongList = songList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateData(song: Song) {
        val position = filteredSongList.indexOf(song)
        filteredSongList[position] = song
        notifyItemChanged(position)
    }

    fun deleteData(song: Song) {
        val position = filteredSongList.indexOf(song)
        filteredSongList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun selectSong(song: Song) {
        if (previousPosition >= 0) {
            filteredSongList[previousPosition].selected = false
            notifyItemChanged(previousPosition)
        }
        val position = filteredSongList.indexOfFirst { it.data == song.data }
        filteredSongList[position].selected = true
        previousPosition = position
        notifyItemChanged(position)
    }

    inner class ViewHolder(itemView: View) : ItemHolder<Song>(itemView) {
        private var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_song_card)
        private val songText: TextView = itemView.findViewById(R.id.item_track_text)
        private val albumText: TextView = itemView.findViewById(R.id.item_album_text)
        private val artistText: TextView = itemView.findViewById(R.id.item_artist_text)
        private val albumArt: CircleImageView = itemView.findViewById(R.id.item_album_art)

        override fun populate(item: Song?) {
            item?.let { song ->
                songText.text = song.title
                albumText.text = song.album
                artistText.text = song.artist
                context.loadUri(song.getAlbumArtUri().toString(), albumArt)
                rootLayout.setOnClickListener {
                    logI("Song clicked: $song")
                    callback.onSongClicked(song)
                }
                if (song.selected) {
                    songText.setTextColor(context.color(R.color.card_title_text_color_selected))
                    albumText.setTextColor(context.color(R.color.card_content_text_color_selected))
                    artistText.setTextColor(context.color(R.color.card_content_text_color_selected))
                    albumArt.borderColor = context.color(R.color.item_circle_border_color_selected)
                } else {
                    songText.setTextColor(context.color(R.color.card_title_text_color))
                    albumText.setTextColor(context.color(R.color.card_content_text_color))
                    artistText.setTextColor(context.color(R.color.card_content_text_color))
                    albumArt.borderColor = context.color(R.color.item_circle_border_color)
                }
            }
        }
    }

    interface SongCallback {
        fun onSongClicked(song: Song)
        fun onSongEdit(song: Song)
        fun onSongDeleted(song: Song)
    }
}
