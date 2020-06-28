package com.zc.phonoplayer.util

import java.util.*

object TimeFormatter {
    private const val HRS_MIN_SEC_FORMAT = "%d:%02d:%02d"
    private const val MIN_SEC_FORMAT = "%02d:%02d"

    fun getSongDuration(timeMs: Int): String? {
        val formatter = Formatter(StringBuilder(), Locale.US)
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            formatter.format(HRS_MIN_SEC_FORMAT, hours, minutes, seconds).toString()
        } else {
            formatter.format(MIN_SEC_FORMAT, minutes, seconds).toString()
        }
    }
}
