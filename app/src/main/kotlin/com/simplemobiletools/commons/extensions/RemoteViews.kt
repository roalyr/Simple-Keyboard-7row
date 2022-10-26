package com.simplemobiletools.commons.extensions

import android.graphics.Color
import android.view.View
import android.widget.RemoteViews

fun RemoteViews.setText(id: Int, text: String) {
    setTextViewText(id, text)
}

fun RemoteViews.applyColorFilter(id: Int, color: Int) {
    setInt(id, "setColorFilter", color)
    setInt(id, "setImageAlpha", Color.alpha(color))
}
