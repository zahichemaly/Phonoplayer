package com.zc.phonoplayer.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore


object SongHelper {
    fun getBitmapFromUri(uri: Uri?, contentResolver: ContentResolver): Bitmap {
        uri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                return ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                return MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } ?: return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    }
}
