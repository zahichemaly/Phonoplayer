package com.zc.phonoplayer.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ItemHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun populate(item: T?)
}
