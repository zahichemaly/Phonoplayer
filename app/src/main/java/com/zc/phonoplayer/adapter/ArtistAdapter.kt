package com.zc.phonoplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Artist
import java.util.*

class ArtistAdapter(artistList: List<Artist>) :
    IndexAdapter<ArtistAdapter.ViewHolder>(artistList.mapNotNull { it.title }) {
    private var filteredArtistList = artistList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredArtistList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = filteredArtistList.getOrNull(position)
        holder.populateViewHolder(artist)
    }

    fun sortBy(sortOrder: SortOrder) {
        when (sortOrder) {
            SortOrder.ASCENDING -> filteredArtistList.sortBy { it.title?.toLowerCase(Locale.US) }
            SortOrder.DESCENDING -> filteredArtistList.sortByDescending { it.title?.toLowerCase(Locale.US) }
            SortOrder.NB_OF_TRACKS -> filteredArtistList.sortByDescending { it.nbOfTracks }
            SortOrder.NB_OF_ALBUMS -> filteredArtistList.sortByDescending { it.nbOfAlbums }
            else -> filteredArtistList
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.item_artist_name)
        private val nbOfTracksTv: TextView = itemView.findViewById(R.id.item_nb_of_tracks)
        private val nbOfAlbumsTv: TextView = itemView.findViewById(R.id.item_nb_of_albums)

        fun populateViewHolder(artist: Artist?) {
            artist?.let {
                titleTv.text = artist.title
                nbOfTracksTv.text = artist.getNbOfTracks()
                nbOfAlbumsTv.text = artist.getNbOfAlbums()
            }
        }
    }
}
