package com.zc.phonoplayer.util

import android.content.Context
import android.view.View
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

fun Context.color(colorInt: Int): Int {
    return ContextCompat.getColor(this, colorInt)
}

inline fun <reified T> Gson.fromJson(json: String) = fromJson<T>(json, object : TypeToken<T>() {}.type)
