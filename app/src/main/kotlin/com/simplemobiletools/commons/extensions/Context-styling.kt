package com.simplemobiletools.commons.extensions

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.view.ViewGroup
import androidx.loader.content.CursorLoader
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.SharedTheme
import com.simplemobiletools.commons.views.*
import com.simplemobiletools.keyboard.R

fun Context.getProperTextColor(): Int  {
    return baseConfig.textColor
}

fun Context.getProperSmalllabelColor(): Int  {
    return baseConfig.smallLabelColor
}

fun Context.getProperBackgroundColor(): Int  {
    return baseConfig.backgroundColor
}

fun Context.getProperKeyColor(): Int {
    return baseConfig.keyColor
}

fun Context.getProperPrimaryColor(): Int {
    return baseConfig.primaryColor
}


fun Context.checkkeyColor() {
    val appId = baseConfig.appId
    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.keyColor) {
        getkeyColors().forEachIndexed { index, color ->
            togglekeyColor(appId, index, color, false)
        }

        getkeyColors().forEachIndexed { index, color ->
            if (baseConfig.keyColor == color) {
                togglekeyColor(appId, index, color, true)
            }
        }
    }
}

fun Context.togglekeyColor(appId: String, colorIndex: Int, color: Int, enable: Boolean) {
    val className =
        "${appId.removeSuffix(".debug")}.activities.SplashActivity${keyColorStrings[colorIndex]}"
    val state =
        if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(
            ComponentName(appId, className),
            state,
            PackageManager.DONT_KILL_APP
        )
        if (enable) {
            baseConfig.lastIconColor = color
        }
    } catch (_: Exception) {
    }
}

fun Context.getkeyColors(): ArrayList<Int> =
    resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())

