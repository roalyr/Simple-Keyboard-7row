package com.simplemobiletools.keyboard.activities

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.keyboard.R
import com.simplemobiletools.keyboard.extensions.config
import com.simplemobiletools.keyboard.helpers.*
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()
        setupCustomizeColors()
        setupManageClipboardItems()
        setupVibrateOnKeypress()
        setupShowPopupOnKeypress()
        setupKeyboardLanguage()
        setupKeyboardHeightMultiplier()

        //arrayOf(settings_color_customization_label, settings_general_settings_label).forEach {
            //it.setTextColor(getProperPrimaryColor())
        //}

        //arrayOf(settings_color_customization_holder, settings_general_settings_holder).forEach {
            //it.background.applyColorFilter(getProperBackgroundColor().getContrastColor())
        //}
    }


    private fun setupCustomizeColors() {
        settings_customize_colors_label.text = getCustomizeColorsString()
        settings_customize_colors_holder.setOnClickListener {
            handleCustomizeColorsClick()
        }
    }


    private fun setupManageClipboardItems() {
        settings_manage_clipboard_items_holder.setOnClickListener {
            Intent(this, ManageClipboardItemsActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupVibrateOnKeypress() {
        settings_vibrate_on_keypress.isChecked = config.vibrateOnKeypress
        settings_vibrate_on_keypress_holder.setOnClickListener {
            settings_vibrate_on_keypress.toggle()
            config.vibrateOnKeypress = settings_vibrate_on_keypress.isChecked
        }
    }

    private fun setupShowPopupOnKeypress() {
        settings_show_popup_on_keypress.isChecked = config.showPopupOnKeypress
        settings_show_popup_on_keypress_holder.setOnClickListener {
            settings_show_popup_on_keypress.toggle()
            config.showPopupOnKeypress = settings_show_popup_on_keypress.isChecked
        }
    }

    private fun setupKeyboardLanguage() {
        settings_keyboard_language.text = getKeyboardLanguageText(config.keyboardLanguage)
        settings_keyboard_language_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(LANGUAGE_ENGLISH_QWERTY, getKeyboardLanguageText(LANGUAGE_ENGLISH_QWERTY)),
                RadioItem(LANGUAGE_UKRAINIAN, getKeyboardLanguageText(LANGUAGE_UKRAINIAN)),
            )

            RadioGroupDialog(this@SettingsActivity, items, config.keyboardLanguage) {
                config.keyboardLanguage = it as Int
                settings_keyboard_language.text = getKeyboardLanguageText(config.keyboardLanguage)
            }
        }
    }

    private fun getKeyboardLanguageText(language: Int): String {
        return when (language) {
            LANGUAGE_UKRAINIAN -> getString(R.string.translation_ukrainian)
            else -> "${getString(R.string.translation_english)} (QWERTY)"
        }
    }

    private fun setupKeyboardHeightMultiplier() {
        settings_keyboard_height_multiplier.text = getKeyboardHeightMultiplierText(config.keyboardHeightMultiplier)
        settings_keyboard_height_multiplier_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(KEYBOARD_HEIGHT_MULTIPLIER_SMALL, getKeyboardHeightMultiplierText(KEYBOARD_HEIGHT_MULTIPLIER_SMALL)),
                RadioItem(KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM, getKeyboardHeightMultiplierText(KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM)),
                RadioItem(KEYBOARD_HEIGHT_MULTIPLIER_LARGE, getKeyboardHeightMultiplierText(KEYBOARD_HEIGHT_MULTIPLIER_LARGE)),
            )

            RadioGroupDialog(this@SettingsActivity, items, config.keyboardHeightMultiplier) {
                config.keyboardHeightMultiplier = it as Int
                settings_keyboard_height_multiplier.text = getKeyboardHeightMultiplierText(config.keyboardHeightMultiplier)
            }
        }
    }

    private fun getKeyboardHeightMultiplierText(multiplier: Int): String {
        return when (multiplier) {
            KEYBOARD_HEIGHT_MULTIPLIER_SMALL -> getString(R.string.small)
            KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM -> getString(R.string.medium)
            KEYBOARD_HEIGHT_MULTIPLIER_LARGE -> getString(R.string.large)
            else -> getString(R.string.small)
        }
    }
}
