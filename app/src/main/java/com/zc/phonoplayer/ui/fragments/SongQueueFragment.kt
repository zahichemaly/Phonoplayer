package com.zc.phonoplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zc.phonoplayer.R

class SongQueueFragment : Fragment() {

    companion object {
        fun newInstance(): SongQueueFragment {
            return SongQueueFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song_queue, container, false)
        return view
    }
}