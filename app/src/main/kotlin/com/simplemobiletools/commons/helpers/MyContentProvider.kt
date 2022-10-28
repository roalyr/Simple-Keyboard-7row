package com.simplemobiletools.commons.helpers

import android.content.ContentValues
import android.net.Uri
import com.simplemobiletools.commons.models.SharedTheme

class MyContentProvider {
    companion object {
        private const val AUTHORITY = "com.simplemobiletools.commons.provider"
        const val SHARED_THEME_ACTIVATED: String = "com.simplemobiletools.commons.SHARED_THEME_ACTIVATED"
        const val SHARED_THEME_UPDATED: String = "com.simplemobiletools.commons.SHARED_THEME_UPDATED"
        val MY_CONTENT_URI: Uri? = Uri.parse("content://$AUTHORITY/themes")

        private const val COL_TEXT_COLOR: String = "text_color"
        private const val COL_BACKGROUND_COLOR: String = "background_color"
        private const val COL_PRIMARY_COLOR: String = "primary_color"
        private const val COL_ACCENT_COLOR: String = "accent_color"
        private const val COL_APP_ICON_COLOR: String = "app_icon_color"
        private const val COL_NAVIGATION_BAR_COLOR: String = "navigation_bar_color"
        private const val COL_LAST_UPDATED_TS: String = "last_updated_ts"

        fun fillThemeContentValues(sharedTheme: SharedTheme): ContentValues = ContentValues().apply {
            put(COL_TEXT_COLOR, sharedTheme.textColor)
            put(COL_BACKGROUND_COLOR, sharedTheme.backgroundColor)
            put(COL_PRIMARY_COLOR, sharedTheme.primaryColor)
            put(COL_ACCENT_COLOR, sharedTheme.accentColor)
            put(COL_APP_ICON_COLOR, sharedTheme.keyColor)
            put(COL_NAVIGATION_BAR_COLOR, sharedTheme.navigationBarColor)
            put(COL_LAST_UPDATED_TS, System.currentTimeMillis() / 1000)
        }
    }
}
