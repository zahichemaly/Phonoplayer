package com.zc.phonoplayer.service.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.source.ShuffleOrder
import java.util.*

class ShuffledOrderFromIndex(private val length: Int, private val startingIndex: Int = 0) : ShuffleOrder {
    private var shuffled: IntArray = IntArray(length)
    private var random: Random = Random()

    constructor(length: Int, startingIndex: Int, random: Random) : this(length, startingIndex) {
        this.random = random
    }

    constructor(shuffled: IntArray, startingIndex: Int, random: Random) : this(shuffled.size, startingIndex, random) {
        this.shuffled = shuffled
    }

    init {
        val randomList = (0 until length).shuffled()
        for (i in 0 until length) {
            shuffled[i] = randomList[i]
        }
    }

    override fun getLength(): Int {
        return length
    }

    override fun getNextIndex(index: Int): Int {
        return if (length > 0) {
            var nextIndex = shuffled.indexOf(index)
            if (++nextIndex < length) {
                shuffled[nextIndex]
            } else {
                shuffled[0]
            }
        } else C.INDEX_UNSET
    }

    override fun getPreviousIndex(index: Int): Int {
        return if (length > 0) {
            var previousIndex = shuffled.indexOf(index)
            if (--previousIndex >= 0) {
                shuffled[previousIndex]
            } else {
                shuffled[length - 1]
            }
        } else C.INDEX_UNSET
    }

    override fun getLastIndex(): Int {
        return if (length > 0) {
            val startingIndexInShuffled = shuffled.indexOf(startingIndex)
            val lastIndex = startingIndexInShuffled - 1
            if (lastIndex < 0) length - 1
            else lastIndex
        } else C.INDEX_UNSET
    }

    override fun getFirstIndex(): Int {
        return if (length > 0) startingIndex else C.INDEX_UNSET
    }

    override fun cloneAndInsert(insertionIndex: Int, insertionCount: Int): ShuffleOrder {
        val insertionPoints = IntArray(insertionCount)
        val insertionValues = IntArray(insertionCount)
        for (i in 0 until insertionCount) {
            insertionPoints[i] = random.nextInt(shuffled.size + 1)
            val swapIndex = random.nextInt(i + 1)
            insertionValues[i] = insertionValues[swapIndex]
            insertionValues[swapIndex] = i + insertionIndex
        }
        Arrays.sort(insertionPoints)
        val newShuffled = IntArray(shuffled.size + insertionCount)
        var indexInOldShuffled = 0
        var indexInInsertionList = 0
        for (i in 0 until shuffled.size + insertionCount) {
            if (indexInInsertionList < insertionCount
                && indexInOldShuffled == insertionPoints[indexInInsertionList]
            ) {
                newShuffled[i] = insertionValues[indexInInsertionList++]
            } else {
                newShuffled[i] = shuffled[indexInOldShuffled++]
                if (newShuffled[i] >= insertionIndex) {
                    newShuffled[i] += insertionCount
                }
            }
        }
        return ShuffledOrderFromIndex(newShuffled, startingIndex, random)
    }

    override fun cloneAndRemove(indexFrom: Int, indexToExclusive: Int): ShuffleOrder {
        val numberOfElementsToRemove = indexToExclusive - indexFrom
        val newShuffled = IntArray(shuffled.size - numberOfElementsToRemove)
        var foundElementsCount = 0
        for (i in shuffled.indices) {
            if (shuffled[i] in indexFrom until indexToExclusive) {
                foundElementsCount++
            } else {
                newShuffled[i - foundElementsCount] = if (shuffled[i] >= indexFrom) shuffled[i] - numberOfElementsToRemove else shuffled[i]
            }
        }
        return ShuffledOrderFromIndex(newShuffled, startingIndex, random)
    }

    override fun cloneAndClear(): ShuffleOrder {
        return ShuffledOrderFromIndex(0, startingIndex, random)
    }
}
