package com.zc.phonoplayer.ui.components.checkableMenu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R

class CheckablePopupMenu(context: Context, menuItems: List<CheckableMenuItem>, callback: CheckableMenuAdapter.Callback, selectedIndex: Int = 0) :
    PopupWindow(context) {

    init {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_checkable_menu, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.menu_recyclerview)
        menuItems[selectedIndex].isChecked = true
        recyclerView.adapter = CheckableMenuAdapter(menuItems, object : CheckableMenuAdapter.Callback {
            override fun onMenuItemClicked(menuItem: CheckableMenuItem) {
                callback.onMenuItemClicked(menuItem)
                dismiss()
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        isFocusable = true
        width = 600
        height = WindowManager.LayoutParams.WRAP_CONTENT
        contentView = view
    }

    fun show(view: View) {
        showAsDropDown(view)
    }
}
