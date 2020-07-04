package com.zc.phonoplayer.adapter

import android.content.Context
import android.content.DialogInterface
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
import com.zc.phonoplayer.util.showConfirmDialog
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SongAdapter(private var songList: List<Song>, private var callback: SongCallback) :
    IndexAdapter<SongAdapter.ViewHolder>(songList.mapNotNull { it.title }) {
    private lateinit var view: View
    private lateinit var context: Context
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
        song?.let { s ->
            holder.songText.text = s.title
            holder.albumText.text = s.album
            holder.artistText.text = s.artist
            context.loadUri(s.getAlbumArtUri().toString(), holder.albumArt)
            holder.rootLayout.setOnClickListener {
                Log.i("SongAdapter", "Song clicked: " + s.title)
                callback.onSongClicked(song)
            }
            holder.rootLayout.setOnCreateContextMenuListener { menu, v, menuInfo ->
                val editMenu = menu.add(0, v.id, 0, context.getString(R.string.edit))
                val deleteMenu = menu.add(0, v.id, 1, context.getString(R.string.delete))
                deleteMenu.setOnMenuItemClickListener {
                    openDeleteDialog(s)
                    true
                }
                editMenu.setOnMenuItemClickListener {
                    callback.onSongEdit(s)
                    true
                }
            }
        }
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

    fun deleteData(song: Song) {
        val position = filteredSongList.indexOf(song)
        filteredSongList.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun openDeleteDialog(song: Song) {
        context.showConfirmDialog(
            title = context.getString(R.string.delete_track),
            message = context.getString(R.string.confirm_delete_song),
            listener = DialogInterface.OnClickListener { dialog, which ->
                callback.onSongDeleted(song)
            })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_song_card)
        val songText: TextView = itemView.findViewById(R.id.item_track_text)
        val albumText: TextView = itemView.findViewById(R.id.item_album_text)
        val artistText: TextView = itemView.findViewById(R.id.item_artist_text)
        val albumArt: CircleImageView = itemView.findViewById(R.id.item_album_art)
    }

    interface SongCallback {
        fun onSongClicked(song: Song)
        fun onSongEdit(song: Song)
        fun onSongDeleted(song: Song)
    }
}
