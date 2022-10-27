package com.simplemobiletools.commons.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import com.simplemobiletools.keyboard.R
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.MyTheme
import com.simplemobiletools.commons.models.SharedTheme
import com.simplemobiletools.commons.views.MyTextView
import kotlinx.android.synthetic.main.activity_customization.*

class CustomizationActivity : BaseSimpleActivity() {
    private val THEME_LIGHT = 0
    private val THEME_DARK = 1
    private val THEME_BLACK_WHITE = 4
    private val THEME_CUSTOM = 5
    private val THEME_SHARED = 6
    private val THEME_WHITE = 7
    private val THEME_AUTO = 8
    private val THEME_SYSTEM = 9    // Material You

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAccentColor = 0
    private var curKeyColor = 0
    private var curSelectedThemeId = 0
    private var originalkeyColor = 0
    private var lastSavePromptTS = 0L
    private var curNavigationBarColor = INVALID_NAVIGATION_BAR_COLOR
    private var hasUnsavedChanges = false
    private var isThankYou = false      // show "Apply colors to all Simple apps" in Simple Thank You itself even with "Hide Google relations" enabled
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null
    private var storedSharedTheme: SharedTheme? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)
        setupToolbar(customization_toolbar, NavigationIcon.Cross)

        if (baseConfig.defaultNavigationBarColor == INVALID_NAVIGATION_BAR_COLOR && baseConfig.navigationBarColor == INVALID_NAVIGATION_BAR_COLOR) {
            baseConfig.defaultNavigationBarColor = window.navigationBarColor
            baseConfig.navigationBarColor = window.navigationBarColor
        }

        setupOptionsMenu()
        refreshMenuItems()
        isThankYou = packageName.removeSuffix(".debug") == "com.simplemobiletools.thankyou"
        initColorVariables()

        if (isThankYouInstalled()) {
            val cursorLoader = getMyContentProviderCursorLoader()
            ensureBackgroundThread {
                try {
                    storedSharedTheme = getSharedThemeSync(cursorLoader)
                    if (storedSharedTheme == null) {
                        baseConfig.isUsingSharedTheme = false
                    } else {
                        baseConfig.wasSharedThemeEverActivated = true
                    }

                    runOnUiThread {
                        setupThemes()
                        val hideGoogleRelations = resources.getBoolean(R.bool.hide_google_relations) && !isThankYou
                        apply_to_all_holder.beVisibleIf(
                            storedSharedTheme == null && curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM && !hideGoogleRelations
                        )
                    }
                } catch (e: Exception) {
                    toast(R.string.update_thank_you)
                    finish()
                }
            }
        } else {
            setupThemes()
            baseConfig.isUsingSharedTheme = false
        }

        val textColor = if (baseConfig.isUsingSystemTheme) {
            getProperTextColor()
        } else {
            baseConfig.textColor
        }

        updateLabelColors(textColor)
        originalkeyColor = baseConfig.keyColor

        if (resources.getBoolean(R.bool.hide_google_relations) && !isThankYou) {
            apply_to_all_holder.beGone()
        }
    }

    override fun onResume() {
        super.onResume()
        setTheme(getThemeId(getCurrentPrimaryColor()))

        if (!baseConfig.isUsingSystemTheme) {
            updateBackgroundColor(getCurrentBackgroundColor())
            updateActionbarColor(getCurrentStatusBarColor())
            updateNavigationBarColor(curNavigationBarColor)
        }

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
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

            put(THEME_AUTO, getAutoThemeColors())
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0, 0))

        }
        setupThemePicker()
        setupColorsPickers()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        //customization_theme.text = getThemeText()
        updateAutoThemeFields()

        /**
        customization_theme_holder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                themePickerClicked()
            } else {
                ConfirmationDialog(this, "", R.string.app_icon_color_warning, R.string.ok, 0) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    themePickerClicked()
                }
            }
        }
        */

       // if (customization_theme.value == getString(R.string.system_default)) {
       //     apply_to_all_holder.beGone()
       // }
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
                    curAccentColor = baseConfig.customAccentColor
                    curNavigationBarColor = baseConfig.customNavigationBarColor
                    curKeyColor = baseConfig.customKeyColor
                    setTheme(getThemeId(curPrimaryColor))
                    updateMenuItemColors(customization_toolbar.menu, true, curPrimaryColor)
                    setupToolbar(customization_toolbar, NavigationIcon.Cross, curPrimaryColor)
                    setupColorsPickers()
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customAccentColor = curAccentColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                    baseConfig.customNavigationBarColor = curNavigationBarColor
                    baseConfig.customKeyColor = curKeyColor
                }
            }
        }

        hasUnsavedChanges = true
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
        updateBackgroundColor(getCurrentBackgroundColor())
        updateActionbarColor(getCurrentStatusBarColor())
        updateNavigationBarColor(curNavigationBarColor, true)
        updateAutoThemeFields()
        updateApplyToAllColors(getCurrentPrimaryColor())

    }

    private fun getAutoThemeColors(): MyTheme {
        val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
        val textColor = if (isUsingSystemDarkTheme) R.color.theme_dark_text_color else R.color.theme_light_text_color
        val backgroundColor = if (isUsingSystemDarkTheme) R.color.theme_dark_background_color else R.color.theme_light_background_color
        return MyTheme(R.string.auto_light_dark_theme, textColor, backgroundColor, R.color.color_primary, R.color.color_primary)
    }

    private fun getCurrentThemeId(): Int {
        if (baseConfig.isUsingSharedTheme) {
            return THEME_SHARED
        } else if ((baseConfig.isUsingSystemTheme && !hasUnsavedChanges) || curSelectedThemeId == THEME_SYSTEM) {
            return THEME_SYSTEM
        } else if (baseConfig.isUsingAutoTheme || curSelectedThemeId == THEME_AUTO) {
            return THEME_AUTO
        }

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
            accentColor = curAccentColor
            keyColor = curKeyColor

            // -1 is used as an invalid value, lets make use of it for white
            navigationBarColor = if (curNavigationBarColor == INVALID_NAVIGATION_BAR_COLOR) {
                -2
            } else {
                curNavigationBarColor
            }
        }

        /** Reused for key color
        val didkeyColorChange = curKeyColor != originalkeyColor
        if (didkeyColorChange) {
            checkkeyColor()
        }
        */

        if (curSelectedThemeId == THEME_SHARED) {
            val newSharedTheme = SharedTheme(curTextColor, curBackgroundColor, curPrimaryColor, curKeyColor, curNavigationBarColor, 0, curAccentColor)
            updateSharedTheme(newSharedTheme)
            Intent().apply {
                action = MyContentProvider.SHARED_THEME_UPDATED
                sendBroadcast(this)
            }
        }

        baseConfig.isUsingSharedTheme = curSelectedThemeId == THEME_SHARED
        baseConfig.shouldUseSharedTheme = curSelectedThemeId == THEME_SHARED
        baseConfig.isUsingAutoTheme = curSelectedThemeId == THEME_AUTO
        baseConfig.isUsingSystemTheme = curSelectedThemeId == THEME_SYSTEM

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
        updateBackgroundColor()
        updateActionbarColor()
        updateNavigationBarColor()
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curAccentColor = baseConfig.accentColor
        curKeyColor = baseConfig.keyColor
        curNavigationBarColor = baseConfig.navigationBarColor
    }

    private fun setupColorsPickers() {
        val textColor = getCurrentTextColor()
        val backgroundColor = getCurrentBackgroundColor()
        val primaryColor = getCurrentPrimaryColor()
        customization_text_color.setFillWithStroke(textColor, backgroundColor)
        customization_primary_color.setFillWithStroke(primaryColor, backgroundColor)
        customization_accent_color.setFillWithStroke(curAccentColor, backgroundColor)
        customization_background_color.setFillWithStroke(backgroundColor, backgroundColor)
        customization_key_color.setFillWithStroke(curKeyColor, backgroundColor)
        customization_navigation_bar_color.setFillWithStroke(curNavigationBarColor, backgroundColor)
        apply_to_all.setTextColor(primaryColor.getContrastColor())

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
        customization_accent_color_holder.setOnClickListener { pickAccentColor() }

        customization_accent_color_holder.beVisible()

        customization_navigation_bar_color_holder.setOnClickListener { pickNavigationBarColor() }
        apply_to_all.setOnClickListener {
            applyToAll()
        }

        customization_key_color_holder.setOnClickListener { pickKeyColor() }

        /** Reuse for key color picker
        customization_key_color_holder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                pickKeyColor()
            } else {
                ConfirmationDialog(this, "", R.string.app_icon_color_warning, R.string.ok, 0) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    pickKeyColor()
                }
            }
        }
        */

    }

    private fun hasColorChanged(old: Int, new: Int) = Math.abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        refreshMenuItems()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateLabelColors(color)
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        updateBackgroundColor(color)
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        updateActionbarColor(color)
        updateApplyToAllColors(color)
    }

    private fun updateApplyToAllColors(newColor: Int) {
        if (newColor == baseConfig.primaryColor && !baseConfig.isUsingSystemTheme) {
            apply_to_all.setBackgroundResource(R.drawable.button_background_rounded)
        } else {
            val applyBackground = resources.getDrawable(R.drawable.button_background_rounded, theme) as RippleDrawable
            (applyBackground as LayerDrawable).findDrawableByLayerId(R.id.button_background_holder).applyColorFilter(newColor)
            apply_to_all.background = applyBackground
        }
    }

    private fun setCurrentNavigationBarColor(color: Int) {
        curNavigationBarColor = color
        updateNavigationBarColor(color, true)
    }


    private fun pickTextColor() {
        ColorPickerDialog(this, curTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextColor, color)) {
                    setCurrentTextColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
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
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    // Use this color picker instead.
    private fun pickPrimaryColor()  {
        ColorPickerDialog(this, curPrimaryColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    // Reuse for main key color instead.
    private fun pickKeyColor()  {
        ColorPickerDialog(this, curKeyColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curKeyColor, color)) {
                    curKeyColor = color
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    /**
    private fun pickKeyColor() {
        LineColorPickerDialog(this, curKeyColor, false, R.array.md_app_icon_colors, getAppIconIDs()) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curKeyColor, color)) {
                    curKeyColor = color
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }
    */

    /**
    private fun pickPrimaryColor() {
        if (!packageName.startsWith("com.simplemobiletools.", true) && baseConfig.appRunCount > 50) {
            finish()
            return
        }

        curPrimaryLineColorPicker = LineColorPickerDialog(this, curPrimaryColor, true, toolbar = customization_toolbar) { wasPositivePressed, color ->
            curPrimaryLineColorPicker = null
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    setTheme(getThemeId(color))
                }
                updateMenuItemColors(customization_toolbar.menu, true, color)
                setupToolbar(customization_toolbar, NavigationIcon.Cross, color)
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
                updateMenuItemColors(customization_toolbar.menu, true, curPrimaryColor)
                setupToolbar(customization_toolbar, NavigationIcon.Cross, curPrimaryColor)
            }
        }
    }
    */

    private fun pickAccentColor() {
        ColorPickerDialog(this, curAccentColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAccentColor, color)) {
                    curAccentColor = color
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun pickNavigationBarColor() {
        ColorPickerDialog(this, curNavigationBarColor, true, true, currentColorCallback = {
            updateNavigationBarColor(it, true)
        }, callback = { wasPositivePressed, color ->
            if (wasPositivePressed) {
                setCurrentNavigationBarColor(color)
                colorChanged()
                updateColorTheme(getUpdatedTheme())
            } else {
                updateNavigationBarColor(curNavigationBarColor, true)
            }
        })
    }



    private fun getUpdatedTheme() = if (curSelectedThemeId == THEME_SHARED) THEME_SHARED else getCurrentThemeId()

    private fun applyToAll() {
        if (isThankYouInstalled()) {
            ConfirmationDialog(this, "", R.string.share_colors_success, R.string.ok, 0) {
                Intent().apply {
                    action = MyContentProvider.SHARED_THEME_ACTIVATED
                    sendBroadcast(this)
                }

                if (!predefinedThemes.containsKey(THEME_SHARED)) {
                    predefinedThemes[THEME_SHARED] = MyTheme(R.string.shared, 0, 0, 0, 0)
                }
                baseConfig.wasSharedThemeEverActivated = true
                apply_to_all_holder.beGone()
                updateColorTheme(THEME_SHARED)
                saveChanges(false)
            }
        } else {
            PurchaseThankYouDialog(this)
        }
    }

    private fun updateLabelColors(textColor: Int) {
        arrayListOf<MyTextView>(
            customization_text_color_label,
            customization_background_color_label,
            customization_primary_color_label,
            customization_accent_color_label,
            customization_key_color_label,
            customization_navigation_bar_color_label
        ).forEach {
            it.setTextColor(textColor)
        }

        val primaryColor = getCurrentPrimaryColor()
        apply_to_all.setTextColor(primaryColor.getContrastColor())
        updateApplyToAllColors(primaryColor)
    }

    private fun getCurrentTextColor(): Int {
        return curTextColor
    }

    private fun getCurrentBackgroundColor(): Int {
        return curBackgroundColor
    }

    private fun getCurrentPrimaryColor(): Int {
       return  curPrimaryColor
    }

    private fun getCurrentStatusBarColor(): Int {
        return curPrimaryColor
    }
}
