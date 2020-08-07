package com.zc.phonoplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Playlist
import com.zc.phonoplayer.util.loadUri
import com.zc.phonoplayer.util.logI
import de.hdodenhof.circleimageview.CircleImageView

class PlaylistAdapter(private var playlists: List<Playlist>, private var callback: PlaylistAdapter.PlaylistCallback) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.populateViewHolder(playlist)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootLayout: RelativeLayout = itemView.findViewById(R.id.item_playlist_card)
        private val titleTv: TextView = itemView.findViewById(R.id.item_playlist_name)
        private val nbOfTracksTv: TextView = itemView.findViewById(R.id.item_playlist_nb_of_tracks)
        private val songArt1: CircleImageView = itemView.findViewById(R.id.item_playlist_art)

        fun populateViewHolder(playlist: Playlist) {
            titleTv.text = playlist.name
            nbOfTracksTv.text = playlist.getDisplayedNbOfTracks()
            if (playlist.songs?.isNotEmpty() == true) {
                val song = playlist.songs!![0]
                context.loadUri(song.getAlbumArtUri().toString(), songArt1)
            }
            rootLayout.setOnClickListener {
                logI("Playlist clicked: $playlist")
                callback.onPlaylistClicked(playlist)
            }
        }
    }

    interface PlaylistCallback {
        fun onPlaylistClicked(playlist: Playlist)
    }
}
