package com.zc.phonoplayer.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.zc.phonoplayer.R
import com.zc.phonoplayer.util.SharedPreferencesUtil
import com.zc.phonoplayer.util.color

class ManageListPreference @JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0) :
    Preference(context, attrs, defStyleAttr) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: TabAdapter
    private lateinit var values: MutableList<TabItem>
    private lateinit var sharedPreferencesUtil: SharedPreferencesUtil

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, 0) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    val adapter = recyclerView.adapter as TabAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    adapter.notifyItemMoved(from, to)
                    return true
                }

                override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
                    super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    val element = values[from]
                    values.removeAt(from)
                    values.add(to, element)
                    setValues(values)
                }

                override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1.0f
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }


    init {
        layoutResource = R.layout.layout_tab_preference
    }

    private fun getValues(): List<TabItem> = sharedPreferencesUtil.getTabItems()
    private fun setValues(values: List<TabItem>) {
        sharedPreferencesUtil.setTabItems(values)
        super.callChangeListener(values)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        sharedPreferencesUtil = SharedPreferencesUtil(context, sharedPreferences)
        val view = holder.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        values = getValues().toMutableList()
        recyclerAdapter = TabAdapter(values)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recyclerAdapter
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    inner class TabAdapter(private val tabItems: MutableList<TabItem>) : RecyclerView.Adapter<TabAdapter.ViewHolder>() {
        private var selectedCount = tabItems.count { it.isSelected }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tabItem = tabItems[position]
            holder.tabText.text = tabItem.text
            if (tabItem.isSelected) {
                holder.tabCheckBox.isChecked = true
                if (selectedCount > 1) {
                    holder.tabText.setTextColor(context.color(R.color.red))
                    holder.tabCheckBox.isEnabled = true
                } else {
                    holder.tabCheckBox.isEnabled = false
                    holder.tabText.setTextColor(context.color(R.color.dark_grey))
                }
                holder.arrangeIcon.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) startDragging(holder)
                    true
                }
            } else {
                holder.tabText.setTextColor(context.color(R.color.dark_grey))
                holder.tabCheckBox.isChecked = false
            }
            holder.tabCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    holder.tabText.setTextColor(context.color(R.color.red))
                    holder.tabCheckBox.isChecked = isChecked
                    setTabItemSelection(holder, isChecked)
                } else if (!isChecked && selectedCount > 1) {
                    holder.tabText.setTextColor(context.color(R.color.dark_grey))
                    holder.tabCheckBox.isChecked = isChecked
                    setTabItemSelection(holder, isChecked)
                } else {
                    //keep old state
                    holder.tabCheckBox.isChecked = !isChecked
                }
            }
        }

        private fun setTabItemSelection(holder: ViewHolder, isSelected: Boolean) {
            if (isSelected) selectedCount++
            else selectedCount--
            tabItems[holder.adapterPosition].isSelected = isSelected
            notifyDataSetChanged()
            setValues(tabItems)
        }

        override fun getItemCount(): Int {
            return tabItems.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tabCheckBox: MaterialCheckBox = itemView.findViewById(R.id.item_tab_checkbox)
            val tabText: TextView = itemView.findViewById(R.id.item_tab_text)
            val arrangeIcon: ImageView = itemView.findViewById(R.id.item_tab_arrange)
        }
    }

    class TabItem(val text: String, var isSelected: Boolean = true)
}
