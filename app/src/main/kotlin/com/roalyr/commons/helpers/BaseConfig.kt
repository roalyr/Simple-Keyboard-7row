package com.roalyr.commons.helpers

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import com.roalyr.simple_7row_keyboard.R
import com.roalyr.commons.extensions.getInternalStoragePath
import com.roalyr.commons.extensions.getSDCardPath
import com.roalyr.commons.extensions.getSharedPrefs
import java.text.SimpleDateFormat
import java.util.*

open class BaseConfig(val context: Context) {
    protected val prefs: SharedPreferences? = context.getSharedPrefs()

    companion object {
        fun newInstance(context: Context): BaseConfig = BaseConfig(context)
    }

    var appRunCount: Int
        get() = prefs!!.getInt(APP_RUN_COUNT, 0)
        set(appRunCount) = prefs!!.edit().putInt(APP_RUN_COUNT, appRunCount).apply()

    var primaryAndroidDataTreeUri: String
        get() = prefs!!.getString(PRIMARY_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs!!.edit().putString(PRIMARY_ANDROID_DATA_TREE_URI, uri).apply()

    var sdAndroidDataTreeUri: String
        get() = prefs!!.getString(SD_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs!!.edit().putString(SD_ANDROID_DATA_TREE_URI, uri).apply()

    var otgAndroidDataTreeUri: String
        get() = prefs!!.getString(OTG_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs!!.edit().putString(OTG_ANDROID_DATA_TREE_URI, uri).apply()

    var primaryAndroidObbTreeUri: String
        get() = prefs!!.getString(PRIMARY_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs!!.edit().putString(PRIMARY_ANDROID_OBB_TREE_URI, uri).apply()

    var sdAndroidObbTreeUri: String
        get() = prefs!!.getString(SD_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs!!.edit().putString(SD_ANDROID_OBB_TREE_URI, uri).apply()

    var otgAndroidObbTreeUri: String
        get() = prefs!!.getString(OTG_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs!!.edit().putString(OTG_ANDROID_OBB_TREE_URI, uri).apply()

    var sdTreeUri: String
        get() = prefs!!.getString(SD_TREE_URI, "")!!
        set(uri) = prefs!!.edit().putString(SD_TREE_URI, uri).apply()

    var OTGTreeUri: String
        get() = prefs!!.getString(OTG_TREE_URI, "")!!
        set(OTGTreeUri) = prefs!!.edit().putString(OTG_TREE_URI, OTGTreeUri).apply()

    var OTGPartition: String
        get() = prefs!!.getString(OTG_PARTITION, "")!!
        set(OTGPartition) = prefs!!.edit().putString(OTG_PARTITION, OTGPartition).apply()

    var OTGPath: String
        get() = prefs!!.getString(OTG_REAL_PATH, "")!!
        set(OTGPath) = prefs!!.edit().putString(OTG_REAL_PATH, OTGPath).apply()

    var sdCardPath: String
        get() = prefs!!.getString(SD_CARD_PATH, getDefaultSDCardPath())!!
        set(sdCardPath) = prefs!!.edit().putString(SD_CARD_PATH, sdCardPath).apply()

    private fun getDefaultSDCardPath() = if (prefs!!.contains(SD_CARD_PATH)) "" else context.getSDCardPath()

    var internalStoragePath: String
        get() = prefs!!.getString(INTERNAL_STORAGE_PATH, getDefaultInternalPath())!!
        set(internalStoragePath) = prefs!!.edit().putString(INTERNAL_STORAGE_PATH, internalStoragePath).apply()

    private fun getDefaultInternalPath() = if (prefs!!.contains(INTERNAL_STORAGE_PATH)) "" else getInternalStoragePath()

    var textColor: Int
        get() = prefs!!.getInt(TEXT_COLOR, context.resources.getColor(R.color.default_text_color))
        set(textColor) = prefs!!.edit().putInt(TEXT_COLOR, textColor).apply()

    var backgroundColor: Int
        get() = prefs!!.getInt(BACKGROUND_COLOR, context.resources.getColor(R.color.default_background_color))
        set(backgroundColor) = prefs!!.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs!!.getInt(PRIMARY_COLOR, context.resources.getColor(R.color.color_primary))
        set(primaryColor) = prefs!!.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    var smallLabelColor: Int
        get() = prefs!!.getInt(ACCENT_COLOR, context.resources.getColor(R.color.color_primary))
        set(accentColor) = prefs!!.edit().putInt(ACCENT_COLOR, accentColor).apply()

    var navigationBarColor: Int
        get() = prefs!!.getInt(NAVIGATION_BAR_COLOR, INVALID_NAVIGATION_BAR_COLOR)
        set(navigationBarColor) = prefs!!.edit().putInt(NAVIGATION_BAR_COLOR, navigationBarColor).apply()

    var defaultNavigationBarColor: Int
        get() = prefs!!.getInt(DEFAULT_NAVIGATION_BAR_COLOR, INVALID_NAVIGATION_BAR_COLOR)
        set(defaultNavigationBarColor) = prefs!!.edit().putInt(DEFAULT_NAVIGATION_BAR_COLOR, defaultNavigationBarColor).apply()

    var keyColor: Int
        get() = prefs!!.getInt(APP_ICON_COLOR, context.resources.getColor(R.color.color_primary))
        set(keyColor) {
            isUsingModifiedAppIcon = keyColor != context.resources.getColor(R.color.color_primary)
            prefs!!.edit().putInt(APP_ICON_COLOR, keyColor).apply()
        }

    var lastIconColor: Int
        get() = prefs!!.getInt(LAST_ICON_COLOR, context.resources.getColor(R.color.color_primary))
        set(lastIconColor) = prefs!!.edit().putInt(LAST_ICON_COLOR, lastIconColor).apply()

    var customTextColor: Int
        get() = prefs!!.getInt(CUSTOM_TEXT_COLOR, textColor)
        set(customTextColor) = prefs!!.edit().putInt(CUSTOM_TEXT_COLOR, customTextColor).apply()

    var customBackgroundColor: Int
        get() = prefs!!.getInt(CUSTOM_BACKGROUND_COLOR, backgroundColor)
        set(customBackgroundColor) = prefs!!.edit().putInt(CUSTOM_BACKGROUND_COLOR, customBackgroundColor).apply()

    var customPrimaryColor: Int
        get() = prefs!!.getInt(CUSTOM_PRIMARY_COLOR, primaryColor)
        set(customPrimaryColor) = prefs!!.edit().putInt(CUSTOM_PRIMARY_COLOR, customPrimaryColor).apply()

    var customSmallLabelColor: Int
        get() = prefs!!.getInt(CUSTOM_ACCENT_COLOR, smallLabelColor)
        set(customAccentColor) = prefs!!.edit().putInt(CUSTOM_ACCENT_COLOR, customAccentColor).apply()

    var customKeyColor: Int
        get() = prefs!!.getInt(CUSTOM_APP_ICON_COLOR, keyColor)
        set(customKeyColor) = prefs!!.edit().putInt(CUSTOM_APP_ICON_COLOR, customKeyColor).apply()

    var customNavigationBarColor: Int
        get() = prefs!!.getInt(CUSTOM_NAVIGATION_BAR_COLOR, INVALID_NAVIGATION_BAR_COLOR)
        set(customNavigationBarColor) = prefs!!.edit().putInt(CUSTOM_NAVIGATION_BAR_COLOR, customNavigationBarColor).apply()

    var useEnglish: Boolean
        get() = prefs!!.getBoolean(USE_ENGLISH, false)
        set(useEnglish) {
            wasUseEnglishToggled = true
            prefs!!.edit().putBoolean(USE_ENGLISH, useEnglish).commit()
        }

    private var wasUseEnglishToggled: Boolean
        get() = prefs!!.getBoolean(WAS_USE_ENGLISH_TOGGLED, false)
        set(wasUseEnglishToggled) = prefs!!.edit().putBoolean(WAS_USE_ENGLISH_TOGGLED, wasUseEnglishToggled).apply()

    var isUsingSharedTheme: Boolean
        get() = prefs!!.getBoolean(IS_USING_SHARED_THEME, false)
        set(isUsingSharedTheme) = prefs!!.edit().putBoolean(IS_USING_SHARED_THEME, isUsingSharedTheme).apply()

    var isUsingSystemTheme: Boolean
        get() = prefs!!.getBoolean(IS_USING_SYSTEM_THEME, false)
        set(isUsingSystemTheme) = prefs!!.edit().putBoolean(IS_USING_SYSTEM_THEME, isUsingSystemTheme).apply()

    var use24HourFormat: Boolean
        get() = prefs!!.getBoolean(USE_24_HOUR_FORMAT, DateFormat.is24HourFormat(context))
        set(use24HourFormat) = prefs!!.edit().putBoolean(USE_24_HOUR_FORMAT, use24HourFormat).apply()

    private var isUsingModifiedAppIcon: Boolean
        get() = prefs!!.getBoolean(IS_USING_MODIFIED_APP_ICON, false)
        set(isUsingModifiedAppIcon) = prefs!!.edit().putBoolean(IS_USING_MODIFIED_APP_ICON, isUsingModifiedAppIcon).apply()

    var appId: String
        get() = prefs!!.getString(APP_ID, "")!!
        set(appId) = prefs!!.edit().putString(APP_ID, appId).apply()

    var wasOrangeIconChecked: Boolean
        get() = prefs!!.getBoolean(WAS_ORANGE_ICON_CHECKED, false)
        set(wasOrangeIconChecked) = prefs!!.edit().putBoolean(WAS_ORANGE_ICON_CHECKED, wasOrangeIconChecked).apply()

    var appSideloadingStatus: Int
        get() = prefs!!.getInt(APP_SIDELOADING_STATUS, SIDELOADING_UNCHECKED)
        set(appSideloadingStatus) = prefs!!.edit().putInt(APP_SIDELOADING_STATUS, appSideloadingStatus).apply()

    var dateFormat: String
        get() = prefs!!.getString(DATE_FORMAT, getDefaultDateFormat())!!
        set(dateFormat) = prefs!!.edit().putString(DATE_FORMAT, dateFormat).apply()

    private fun getDefaultDateFormat(): String {
        val format = DateFormat.getDateFormat(context)
        val pattern = (format as SimpleDateFormat).toLocalizedPattern()
        return when (pattern.lowercase(Locale.getDefault()).replace(" ", "")) {
            "d.M.y" -> DATE_FORMAT_ONE
            "dd/mm/y" -> DATE_FORMAT_TWO
            "mm/dd/y" -> DATE_FORMAT_THREE
            "y-mm-dd" -> DATE_FORMAT_FOUR
            "dmmmmy" -> DATE_FORMAT_FIVE
            "mmmmdy" -> DATE_FORMAT_SIX
            "mm-dd-y" -> DATE_FORMAT_SEVEN
            "dd-mm-y" -> DATE_FORMAT_EIGHT
            else -> DATE_FORMAT_ONE
        }
    }

    var fontSize: Int
        get() = prefs!!.getInt(FONT_SIZE, context.resources.getInteger(R.integer.default_font_size))
        set(size) = prefs!!.edit().putInt(FONT_SIZE, size).apply()

    var favorites: MutableSet<String>
        get() = prefs!!.getStringSet(FAVORITES, HashSet())!!
        set(favorites) = prefs!!.edit().remove(FAVORITES).putStringSet(FAVORITES, favorites).apply()

    // color picker last used colors
    internal var colorPickerRecentColors: LinkedList<Int>
        get(): LinkedList<Int> {
            val defaultList = arrayListOf(
                context.resources.getColor(R.color.md_red_700),
                context.resources.getColor(R.color.md_blue_700),
                context.resources.getColor(R.color.md_green_700),
                context.resources.getColor(R.color.md_yellow_700),
                context.resources.getColor(R.color.md_orange_700)
            )
            return LinkedList(prefs!!.getString(COLOR_PICKER_RECENT_COLORS, null)?.lines()?.map { it.toInt() } ?: defaultList)
        }
        set(recentColors) = prefs!!.edit().putString(COLOR_PICKER_RECENT_COLORS, recentColors.joinToString(separator = "\n")).apply()
}
