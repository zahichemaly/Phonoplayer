package com.zc.phonoplayer.ui.components

import `in`.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView
import android.content.Context
import android.util.AttributeSet

class IndexedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : IndexFastScrollRecyclerView(context, attrs, defStyleAttr) {

    init {
        isVerticalScrollBarEnabled = true
        setIndexBarCornerRadius(25)
        setIndexbarMargin(0f)
    }
}
