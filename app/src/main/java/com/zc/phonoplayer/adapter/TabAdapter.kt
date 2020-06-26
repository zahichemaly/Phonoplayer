package com.zc.phonoplayer.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.*

@Suppress("DEPRECATION")
class TabAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm!!) {
    private val mFragmentList: MutableList<Fragment> = ArrayList()
    private val mFragmentTitleList: MutableList<String> = ArrayList()
    private var mCurrentFragment: Fragment? = null

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun getCurrentFragment(): Fragment? {
        return mCurrentFragment
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    fun getFragment(index: Int): Fragment {
        return mFragmentList[index]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        if (mCurrentFragment != obj) {
            mCurrentFragment = obj as Fragment

        }
        super.setPrimaryItem(container, position, obj)
    }
}
