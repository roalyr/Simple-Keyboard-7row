package com.simplemobiletools.commons.activities

import android.os.Bundle
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.MyTheme
import com.simplemobiletools.keyboard.R
import kotlinx.android.synthetic.main.activity_customization.*
import kotlin.math.abs

class CustomizationActivity : BaseSimpleActivity() {
    private val THEME_CUSTOM = 5
    private val THEME_SHARED = 6
    private val THEME_AUTO = 8
    private val THEME_SYSTEM = 9    // Material You

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curSmallLabelColor = 0
    private var curKeyColor = 0
    private var curSelectedThemeId = 0
    private var originalkeyColor = 0
    private var lastSavePromptTS = 0L
    private var curNavigationBarColor = INVALID_NAVIGATION_BAR_COLOR
    private var hasUnsavedChanges = false
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null

    override fun getAppIconIDs(): java.util.ArrayList<Int> = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName(): String = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        if (baseConfig.defaultNavigationBarColor == INVALID_NAVIGATION_BAR_COLOR && baseConfig.navigationBarColor == INVALID_NAVIGATION_BAR_COLOR) {
            baseConfig.defaultNavigationBarColor = window.navigationBarColor
            baseConfig.navigationBarColor = window.navigationBarColor
        }

        setupOptionsMenu()
        refreshMenuItems()
        initColorVariables()

        setupThemes()
        baseConfig.isUsingSharedTheme = false
        baseConfig.textColor

        originalkeyColor = baseConfig.keyColor
    }

    override fun onResume() {
        super.onResume()
        setTheme(curPrimaryColor)

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            setTheme(getThemeId(this))
        }
    }

    private fun refreshMenuItems() {
        customization_toolbar.menu.findItem(R.id.save).isVisible = hasUnsavedChanges
    }

    private fun setupOptionsMenu() {
        customization_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    saveChanges(true)
                    true
                }
                else -> false
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (hasUnsavedChanges && System.currentTimeMillis() - lastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL) {
            promptSaveDiscard()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0, 0))
        }
        setupThemePicker()
        setupColorsPickers()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        updateAutoThemeFields()

    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        // customization_theme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    curSmallLabelColor = baseConfig.customSmallLabelColor
                    curNavigationBarColor = baseConfig.customNavigationBarColor
                    curKeyColor = baseConfig.customKeyColor
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customSmallLabelColor = curSmallLabelColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                    baseConfig.customNavigationBarColor = curNavigationBarColor
                    baseConfig.customKeyColor = curKeyColor
                }
            }
        }

        hasUnsavedChanges = true
        refreshMenuItems()
        updateAutoThemeFields()
    }

    private fun getCurrentThemeId(): Int {
        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != THEME_CUSTOM && it.key != THEME_SHARED && it.key != THEME_AUTO && it.key != THEME_SYSTEM }) {
                if (curTextColor == getColor(value.textColorId) &&
                    curBackgroundColor == getColor(value.backgroundColorId) &&
                    curPrimaryColor == getColor(value.primaryColorId) &&
                    curKeyColor == getColor(value.keyColorId) &&
                    (curNavigationBarColor == baseConfig.defaultNavigationBarColor || curNavigationBarColor == -2)
                ) {
                    themeId = key
                }
            }
        }
        return themeId
    }

    private fun updateAutoThemeFields() {
        arrayOf(customization_text_color_holder, customization_background_color_holder, customization_navigation_bar_color_holder).forEach {
            it.beVisibleIf(curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM)
        }

        customization_primary_color_holder.beVisibleIf(curSelectedThemeId != THEME_SYSTEM)
    }

    private fun promptSaveDiscard() {
        lastSavePromptTS = System.currentTimeMillis()
        ConfirmationAdvancedDialog(this, "", R.string.save_before_closing, R.string.save, R.string.discard) {
            if (it) {
                saveChanges(true)
            } else {
                resetColors()
                finish()
            }
        }
    }

    private fun saveChanges(finishAfterSave: Boolean) {

        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
            smallLabelColor = curSmallLabelColor
            keyColor = curKeyColor

            // -1 is used as an invalid value, lets make use of it for white
            navigationBarColor = if (curNavigationBarColor == INVALID_NAVIGATION_BAR_COLOR) {
                -2
            } else {
                curNavigationBarColor
            }
        }

        hasUnsavedChanges = false
        if (finishAfterSave) {
            finish()
        } else {
            refreshMenuItems()
        }
    }

    private fun resetColors() {
        hasUnsavedChanges = false
        initColorVariables()
        setupColorsPickers()
        refreshMenuItems()
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curSmallLabelColor = baseConfig.smallLabelColor
        curKeyColor = baseConfig.keyColor
        curNavigationBarColor = baseConfig.navigationBarColor
    }

    private fun setupColorsPickers() {

        customization_text_color.setFillWithStroke(curTextColor, curBackgroundColor)
        customization_primary_color.setFillWithStroke(curPrimaryColor, curBackgroundColor)
        customization_small_label_color.setFillWithStroke(curSmallLabelColor, curBackgroundColor)
        customization_background_color.setFillWithStroke(curBackgroundColor, curBackgroundColor)
        customization_key_color.setFillWithStroke(curKeyColor, curBackgroundColor)
        customization_navigation_bar_color.setFillWithStroke(curNavigationBarColor, curBackgroundColor)

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
        customization_color_small_label_holder.setOnClickListener { pickSmallLabelColor() }
        customization_navigation_bar_color_holder.setOnClickListener { pickNavigationBarColor() }
        customization_key_color_holder.setOnClickListener { pickKeyColor() }

    }

    private fun hasColorChanged(old: Int, new: Int) = abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        refreshMenuItems()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
    }

    private fun setCurrentNavigationBarColor(color: Int) {
        curNavigationBarColor = color
    }


    private fun pickTextColor() {
        ColorPickerDialog(this, curTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextColor, color)) {
                    setCurrentTextColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    setCurrentBackgroundColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickPrimaryColor() {
        ColorPickerDialog(this, curPrimaryColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickKeyColor() {
        ColorPickerDialog(this, curKeyColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curKeyColor, color)) {
                    curKeyColor = color
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickSmallLabelColor() {
        ColorPickerDialog(this, curSmallLabelColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curSmallLabelColor, color)) {
                    curSmallLabelColor = color
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickNavigationBarColor() {
        ColorPickerDialog(this, curNavigationBarColor, true, true, currentColorCallback = {
        }, callback = { wasPositivePressed, color ->
            if (wasPositivePressed) {
                setCurrentNavigationBarColor(color)
                colorChanged()
                updateColorTheme(getCurrentThemeId())
            }
        })
    }
}
