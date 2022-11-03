package com.roalyr.simple_7row_keyboard.activities

import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import com.roalyr.commons.extensions.*
import com.roalyr.commons.helpers.LICENSE_GSON
import com.roalyr.simple_7row_keyboard.BuildConfig
import com.roalyr.simple_7row_keyboard.R
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

}
