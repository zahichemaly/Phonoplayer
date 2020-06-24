package com.zc.phonoplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Artist

class ArtistAdapter(private var artistList: List<Artist>) :
    IndexAdapter<ArtistAdapter.ViewHolder>(artistList.mapNotNull { it.title }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return artistList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = artistList[position]
        holder.populateViewHolder(artist)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.item_artist_name)
        private val nbOfTracksTv: TextView = itemView.findViewById(R.id.item_nb_of_tracks)
        private val nbOfAlbumsTv: TextView = itemView.findViewById(R.id.item_nb_of_albums)

        fun populateViewHolder(artist: Artist) {
            titleTv.text = artist.title
            nbOfTracksTv.text = artist.getNbOfTracks()
            nbOfAlbumsTv.text = artist.getNbOfAlbums()
        }
    }
}
