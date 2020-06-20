package com.zc.phonoplayer.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zc.phonoplayer.R
import de.hdodenhof.circleimageview.CircleImageView

fun View.loadUri(drawable: String?, imageView: CircleImageView) {
    Glide.with(this)
        .load(drawable)
        .placeholder(R.drawable.ic_default_music)
        .into(imageView)
}

fun Context.loadUri(drawable: String?, imageView: CircleImageView) {
    Glide.with(this)
        .load(drawable)
        .placeholder(R.drawable.ic_default_music)
        .into(imageView)
}

fun Context.color(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object : TypeToken<T>() {}.type)
