package com.zc.phonoplayer.ui.activities

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.zc.phonoplayer.adapter.*

abstract class BaseActivity : AppCompatActivity(), SongAdapter.SongCallback, AlbumAdapter.AlbumCallback, ArtistAdapter.ArtistCallback,
    GenreAdapter.GenreCallback, PlaylistAdapter.PlaylistCallback {

    protected fun addFragment(@IdRes idRes: Int, fragment: Fragment, actionBarTitle: String?, withStateLoss: Boolean = false) {
        setupActionBar(actionBarTitle, true)
        val transaction = supportFragmentManager.beginTransaction().add(idRes, fragment)
        if (withStateLoss) transaction.commitAllowingStateLoss()
        else transaction.commit()
    }

    protected fun removeFragment(@IdRes idRes: Int): Boolean {
        val fragment = supportFragmentManager.findFragmentById(idRes)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
            return true
        }
        return false
    }

    protected fun setupActionBar(actionBarTitle: String?, showBackButton: Boolean) {
        supportActionBar?.run {
            title = actionBarTitle
            setDisplayHomeAsUpEnabled(showBackButton)
            setDisplayShowHomeEnabled(showBackButton)
        }
    }
}
