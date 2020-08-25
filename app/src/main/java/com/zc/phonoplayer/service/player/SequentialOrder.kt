package com.zc.phonoplayer.service.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.source.ShuffleOrder

class SequentialOrder(private val length: Int, private val startingIndex: Int) : ShuffleOrder {
    override fun getLength(): Int {
        return length
    }

    override fun getNextIndex(index: Int): Int {
        return if (length > 0) {
            var nextIndex = index
            if (++nextIndex < length) nextIndex else 0
        } else C.INDEX_UNSET
    }

    override fun getPreviousIndex(index: Int): Int {
        return if (length > 0) {
            var previousIndex = index
            if (--previousIndex >= 0) previousIndex else length - 1
        } else C.INDEX_UNSET
    }

    override fun getLastIndex(): Int {
        return if (length > 0) {
            val lastIndex = startingIndex - 1
            if (lastIndex < 0) length - 1
            else lastIndex
        } else C.INDEX_UNSET
    }

    override fun getFirstIndex(): Int {
        return if (length > 0) startingIndex else C.INDEX_UNSET
    }

    override fun cloneAndInsert(insertionIndex: Int, insertionCount: Int): ShuffleOrder {
        return SequentialOrder(length + insertionCount, startingIndex)
    }

    override fun cloneAndRemove(indexFrom: Int, indexToExclusive: Int): ShuffleOrder {
        return SequentialOrder(length - indexToExclusive + indexFrom, startingIndex)
    }

    override fun cloneAndClear(): ShuffleOrder {
        return SequentialOrder(0, startingIndex)
    }
}
