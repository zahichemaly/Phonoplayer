package com.zc.phonoplayer.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.SectionIndexer

class IndexAdapter(context: Context, objects: ArrayList<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_expandable_list_item_1, objects), SectionIndexer {

    private var sections = emptyArray<String>()
    private var elements = arrayListOf<String>()
    var sectionLetters = mutableListOf<String>()

    init {
        this.elements = objects
        elements.forEach { e ->
            val ch = e[0].toUpperCase().toString()
            sectionLetters.add(ch)
        }
        val sectionList = ArrayList<String>(sectionLetters)
        sectionList.toArray(sections)
    }

    override fun getSections(): Array<String> {
        return sections
    }

    override fun getSectionForPosition(position: Int): Int {
        return position
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return sectionIndex
    }
}