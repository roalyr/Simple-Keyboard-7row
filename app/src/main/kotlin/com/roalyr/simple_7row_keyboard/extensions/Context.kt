package com.roalyr.simple_7row_keyboard.extensions

import android.content.ClipboardManager
import android.content.Context
import com.roalyr.simple_7row_keyboard.R
import com.roalyr.simple_7row_keyboard.databases.ClipsDatabase
import com.roalyr.simple_7row_keyboard.helpers.Config
import com.roalyr.simple_7row_keyboard.interfaces.ClipsDao

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.clipsDB: ClipsDao get() = ClipsDatabase.getInstance(applicationContext).ClipsDao()

fun Context.getCurrentClip(): String? {
    val clipboardManager = (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
    return clipboardManager.primaryClip?.getItemAt(0)?.text?.trim()?.toString()
}

fun Context.getStrokeColor(): Int {
    return resources.getColor(R.color.divider_grey, theme)
}
