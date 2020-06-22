package com.zc.phonoplayer.adapter

import android.widget.SectionIndexer
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

abstract class IndexAdapter<T>(private var mDataArray: List<String>) :
    RecyclerView.Adapter<T>(), SectionIndexer where T : RecyclerView.ViewHolder {
    private lateinit var mSectionPositions: ArrayList<Int>

    override fun getSections(): Array<String> {
        val sections: ArrayList<String> = ArrayList(26)
        mSectionPositions = ArrayList(26)
        mDataArray.forEachIndexed { index, data ->
            val section = data[0].toString().toUpperCase(Locale.US)
            if (!sections.contains(section)) {
                sections.add(section)
                mSectionPositions.add(index)
            }
        }
        return sections.toTypedArray()
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions[sectionIndex]
    }
}
