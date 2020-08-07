package com.zc.phonoplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Genre
import com.zc.phonoplayer.model.Playlist
import com.zc.phonoplayer.util.logI

class GenreAdapter(private var genreList: List<Genre>, private var callback: GenreAdapter.GenreCallback) :
    RecyclerView.Adapter<GenreAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return genreList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genre = genreList[position]
        holder.titleTv.text = genre.name
        holder.rootLayout.setOnClickListener {
            logI("Genre clicked: $genre")
            callback.onGenreClicked(genre) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: RelativeLayout = itemView.findViewById(R.id.item_genre_card)
        val titleTv: TextView = itemView.findViewById(R.id.item_genre_name)
    }

    interface GenreCallback {
        fun onGenreClicked(genre: Genre)
    }
}
