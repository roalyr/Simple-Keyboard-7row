package com.simplemobiletools.commons.views.bottomactionmenu

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.windowManager
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.keyboard.R

class BottomActionMenuView : LinearLayout {
    companion object;

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val inflater = LayoutInflater.from(context)
    private val itemsLookup = LinkedHashMap<Int, BottomActionMenuItem>()
    private val items: List<BottomActionMenuItem>
        get() = itemsLookup.values.toList().sortedWith(compareByDescending<BottomActionMenuItem> {
            it.showAsAction
        }.thenBy {
            it.icon != View.NO_ID
        }).filter { it.isVisible }

    private var callback: BottomActionMenuCallback? = null
    private var itemPopup: BottomActionMenuItemPopup? = null

    init {
        orientation = HORIZONTAL
        elevation = 2f
    }

    fun add(item: BottomActionMenuItem) {
        setItem(item)
    }

    private fun setItem(item: BottomActionMenuItem?) {
        item?.let {
            val oldItem = itemsLookup[item.id]
            itemsLookup[item.id] = item
            if (oldItem != item) {
                init()
            }
        }
    }

    private fun init() {
        removeAllViews()
        val maxItemsBeforeOverflow = computeMaxItemsBeforeOverflow()
        val allItems = items
        for (i in allItems.indices) {
            if (i <= maxItemsBeforeOverflow) {
                drawNormalItem(allItems[i])
            } else {
                drawOverflowItem(allItems.slice(i until allItems.size))
                break
            }
        }
    }

    private fun computeMaxItemsBeforeOverflow(): Int {
        val itemsToShowAsAction = items.filter { it.showAsAction && it.icon != View.NO_ID }
        val itemMinWidth = context.resources.getDimensionPixelSize(R.dimen.cab_item_min_width)
        val totalActionWidth = (itemsToShowAsAction.size + 1) * itemMinWidth
        val screenWidth = if (isRPlus()) {
            context.windowManager.currentWindowMetrics.bounds.width()
        } else {
            context.windowManager.defaultDisplay.width
        }
        val result = if (screenWidth > totalActionWidth) {
            itemsToShowAsAction.size
        } else {
            screenWidth / itemMinWidth
        }
        return result - 1
    }

    private fun drawNormalItem(item: BottomActionMenuItem) {
        (inflater.inflate(R.layout.item_action_mode, this, false) as ImageView).apply {
            setupItem(item)
            setOnClickListener {
                if (itemPopup?.isShowing == true) {
                    itemPopup?.dismiss()
                } else {
                    callback?.onItemClicked(item)
                }
            }
            setOnLongClickListener {
                context.toast(item.title)
                true
            }
            addView(this)
        }
    }

    private fun drawOverflowItem(overFlowItems: List<BottomActionMenuItem>) {
        (inflater.inflate(R.layout.item_action_mode, this, false) as ImageView).apply {
            setImageResource(R.drawable.ic_three_dots_vector)
            val contentDesc = context.getString(R.string.more_info)
            contentDescription = contentDesc
            applyColorFilter(Color.WHITE)
            itemPopup = getOverflowPopup(overFlowItems)
            setOnClickListener {
                if (itemPopup?.isShowing == true) {
                    itemPopup?.dismiss()
                } else {
                    itemPopup?.show(it)
                }
            }
            setOnLongClickListener {
                context.toast(contentDesc)
                true
            }
            addView(this)
        }
    }

    private fun ImageView.setupItem(item: BottomActionMenuItem) {
        id = item.id
        contentDescription = item.title
        if (item.icon != View.NO_ID) {
            setImageResource(item.icon)
        }
        beVisibleIf(item.isVisible)
        applyColorFilter(Color.WHITE)
    }

    private fun getOverflowPopup(overFlowItems: List<BottomActionMenuItem>): BottomActionMenuItemPopup {
        return BottomActionMenuItemPopup(context, overFlowItems) {
            callback?.onItemClicked(it)
        }
    }
}
