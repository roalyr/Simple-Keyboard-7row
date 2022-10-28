package com.simplemobiletools.commons.adapters

import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.keyboard.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getTextSize
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.filepicker_favorite.view.*

class FilepickerFavoritesAdapter(
    activity: BaseSimpleActivity, private val paths: List<String>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private var fontSize = 0f

    init {
        fontSize = activity.getTextSize()
    }

    override fun getActionMenuId(): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = createViewHolder(R.layout.filepicker_favorite, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = paths[position]
        holder.bindView(path, true, false) { itemView, adapterPosition ->
            setupView(itemView, path)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount(): Int = paths.size

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount(): Int = paths.size

    override fun getIsItemSelectable(position: Int): Boolean = false

    override fun getItemKeyPosition(key: Int): Int = paths.indexOfFirst { it.hashCode() == key }

    override fun getItemSelectionKey(position: Int): Int = paths[position].hashCode()

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    private fun setupView(view: View, path: String) {
        view.apply {
            filepicker_favorite_label.text = path
            filepicker_favorite_label.setTextColor(textColor)
            filepicker_favorite_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
        }
    }
}
