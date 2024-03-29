package com.zc.phonoplayer.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zc.phonoplayer.R
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


fun Context.loadUri(drawable: String?, imageView: CircleImageView) {
    Glide.with(this)
        .load(drawable)
        .placeholder(R.drawable.ic_default_music)
        .error(R.drawable.ic_default_music)
        .into(imageView)
}

fun Context.color(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.drawable(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

fun Context.showConfirmDialog(title: String, message: String, listener: DialogInterface.OnClickListener) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton(this.getString(R.string.no)) { _, _ -> }
        .setPositiveButton(this.getString(R.string.yes), listener)
        .setCancelable(false)
        .show()
}

fun Context.showMenuPopup(view: View, @MenuRes menuRes: Int, listener: PopupMenu.OnMenuItemClickListener) {
    val popup = PopupMenu(this, view)
    popup.menuInflater.inflate(menuRes, popup.menu)
    popup.setOnMenuItemClickListener(listener)
    popup.show()
}

fun Activity.showSnackbar(text: String) {
    val snack = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT)
    val params = snack.view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(12, 12, 12, 12)
    snack.view.layoutParams = params
    snack.view.background = drawable(R.drawable.bg_snackbar)
    ViewCompat.setElevation(snack.view, 6f)
    snack.show()
}

inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> List<T>.toArrayList(): ArrayList<T> = this as ArrayList<T>
