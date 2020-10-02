package com.zc.phonoplayer.ui.components.checkableMenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zc.phonoplayer.R

class CheckableMenuAdapter(private val menuItems: List<CheckableMenuItem>, val callback: Callback) :
    RecyclerView.Adapter<CheckableMenuAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_checkable_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.titleTv.text = menuItem.title
        holder.checkmarkIv.isVisible = menuItem.isChecked
        holder.rootLayout.setOnClickListener {
            menuItem.isChecked = !menuItem.isChecked
            refreshMenuItemsSelection(position)
            callback.onMenuItemClicked(menuItem)
        }
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    private fun refreshMenuItemsSelection(position: Int) {
        menuItems.forEachIndexed { index, item -> item.isChecked = index == position }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: RelativeLayout = itemView.findViewById(R.id.item_menu_layout)
        val titleTv: TextView = itemView.findViewById(R.id.item_menu_text)
        val checkmarkIv: ImageView = itemView.findViewById(R.id.item_menu_checkmark)
    }

    interface Callback {
        fun onMenuItemClicked(menuItem: CheckableMenuItem)
    }
}
