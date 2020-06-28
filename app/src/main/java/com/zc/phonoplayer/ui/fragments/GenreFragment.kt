package com.zc.phonoplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R
import com.zc.phonoplayer.adapter.GenreAdapter
import com.zc.phonoplayer.model.Genre
import com.zc.phonoplayer.util.GENRE_LIST

class GenreFragment : Fragment() {
    private lateinit var genreList: ArrayList<Genre>
    private lateinit var callback: GenreAdapter.GenreCallback
    private lateinit var recyclerAdapter: GenreAdapter
    private lateinit var recyclerView: RecyclerView

    companion object {
        fun newInstance(genreList: ArrayList<Genre>): GenreFragment {
            val frag = GenreFragment()
            val args = Bundle()
            args.putParcelableArrayList(GENRE_LIST, genreList)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        genreList = arguments?.getParcelableArrayList(GENRE_LIST) ?: ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_genre, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        val emptyText: TextView = view.findViewById(R.id.empty_songs_text)
        if (genreList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            recyclerView.isNestedScrollingEnabled = true
            recyclerAdapter = GenreAdapter(genreList, callback)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        }
        return view
    }

    fun setGenreCallback(callback: GenreAdapter.GenreCallback) {
        this.callback = callback
    }
}
