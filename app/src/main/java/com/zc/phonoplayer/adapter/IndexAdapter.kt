package com.zc.phonoplayer.adapter

import android.widget.SectionIndexer
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

abstract class IndexAdapter<T>(protected var mDataList: List<String>) :
    RecyclerView.Adapter<T>(), SectionIndexer where T : RecyclerView.ViewHolder {
    private lateinit var mSectionPositions: ArrayList<Int>

    override fun getSections(): Array<String> {
        mSectionPositions = ArrayList(26)
        val sections: ArrayList<String> = ArrayList(26)
        mSectionPositions = ArrayList(26)
        mDataList.forEachIndexed { index, data ->
            val firstCharacter = data.first()
            val section = firstCharacter.toString().toUpperCase(Locale.US)
            if (!sections.contains(section) && firstCharacter.isAlphabet()) {
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

    private fun Char.isAlphabet(): Boolean {
        return (this in 'a'..'z') || (this in 'A'..'Z')
    }
}
