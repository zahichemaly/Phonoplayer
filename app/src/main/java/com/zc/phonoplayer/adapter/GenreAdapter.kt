package com.zc.phonoplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.model.Genre
import com.zc.phonoplayer.util.logI

class GenreAdapter(private var genreList: List<Genre>, private var callback: GenreCallback) :
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
        holder.populate(genre)
    }

    inner class ViewHolder(itemView: View) : ItemHolder<Genre>(itemView) {
        private val rootLayout: RelativeLayout = itemView.findViewById(R.id.item_genre_card)
        private val titleTv: TextView = itemView.findViewById(R.id.item_genre_name)

        override fun populate(item: Genre?) {
            item?.let { genre ->
                titleTv.text = genre.name
                rootLayout.setOnClickListener {
                    logI("Genre clicked: $genre")
                    callback.onGenreClicked(genre)
                }
            }
        }
    }

    interface GenreCallback {
        fun onGenreClicked(genre: Genre)
    }
}
