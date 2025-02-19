package com.roalyr.simple_7row_keyboard.helpers

import android.content.Context
import com.roalyr.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context): Config = Config(context)
    }

    var vibrateOnKeypress: Boolean
        get() = prefs!!.getBoolean(VIBRATE_ON_KEYPRESS, true)
        set(vibrateOnKeypress) = prefs!!.edit().putBoolean(VIBRATE_ON_KEYPRESS, vibrateOnKeypress).apply()

    var showPopupOnKeypress: Boolean
        get() = prefs!!.getBoolean(SHOW_POPUP_ON_KEYPRESS, true)
        set(showPopupOnKeypress) = prefs!!.edit().putBoolean(SHOW_POPUP_ON_KEYPRESS, showPopupOnKeypress).apply()

    var lastExportedClipsFolder: String
        get() = prefs!!.getString(LAST_EXPORTED_CLIPS_FOLDER, "")!!
        set(lastExportedClipsFolder) = prefs!!.edit().putString(LAST_EXPORTED_CLIPS_FOLDER, lastExportedClipsFolder).apply()

    var keyboardLanguage: Int
        get() = prefs!!.getInt(KEYBOARD_LANGUAGE, getDefaultLanguage())
        set(keyboardLanguage) = prefs!!.edit().putInt(KEYBOARD_LANGUAGE, keyboardLanguage).apply()

    var keyboardHeightMultiplier: Int
        get() = prefs!!.getInt(HEIGHT_MULTIPLIER, 1)
        set(keyboardHeightMultiplier) = prefs!!.edit().putInt(HEIGHT_MULTIPLIER, keyboardHeightMultiplier).apply()


    private fun getDefaultLanguage(): Int {
        val conf = context.resources.configuration
        return LANGUAGE_ENGLISH_QWERTY
    }
}
