package com.simplemobiletools.keyboard.activities

import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import com.simplemobiletools.commons.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.LICENSE_GSON
import com.simplemobiletools.keyboard.BuildConfig
import com.simplemobiletools.keyboard.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupOptionsMenu()
        appLaunched(BuildConfig.APPLICATION_ID)
        change_keyboard_holder.setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }
        change_input_method_holder.setOnClickListener {
            Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //updateChangeKeyboardColor()
    }

    private fun setupOptionsMenu() {
        main_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val licenses = LICENSE_GSON
        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, true)
    }

    private fun updateChangeKeyboardColor() {
        val applyBackground = resources.getDrawable(R.drawable.button_background_rounded, theme) as RippleDrawable
        (applyBackground as LayerDrawable).findDrawableByLayerId(R.id.button_background_holder).applyColorFilter(getProperPrimaryColor())
        change_keyboard.background = applyBackground
        change_keyboard.setTextColor(getProperPrimaryColor().getContrastColor())
    }

    private fun isKeyboardEnabled(): Boolean {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledKeyboards = inputMethodManager.enabledInputMethodList
        return enabledKeyboards.any {
            it.settingsActivity == SettingsActivity::class.java.canonicalName
        }
    }
}
