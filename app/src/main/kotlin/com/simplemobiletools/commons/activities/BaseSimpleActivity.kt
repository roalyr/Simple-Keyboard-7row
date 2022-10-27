package com.simplemobiletools.commons.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.appbar.MaterialToolbar
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.dialogs.WritePermissionDialog.Mode
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.keyboard.R

abstract class BaseSimpleActivity : AppCompatActivity() {
    var copyMoveCallback: ((destinationPath: String) -> Unit)? = null
    var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    var isAskingPermissions = false
    var useDynamicTheme = true
    var showTransparentTop = false
    var showTransparentNavigation = false
    var checkedDocumentPath = ""
    var configItemsToExport = LinkedHashMap<String, Any>()

    private val GENERIC_PERM_HANDLER = 100
    private val DELETE_FILE_SDK_30_HANDLER = 300
    private val RECOVERABLE_SECURITY_HANDLER = 301
    private val UPDATE_FILE_SDK_30_HANDLER = 302
    private val MANAGE_MEDIA_RC = 303

    companion object {
        var funAfterSAFPermission: ((success: Boolean) -> Unit)? = null
        var funAfterSdk30Action: ((success: Boolean) -> Unit)? = null
        var funAfterUpdate30File: ((success: Boolean) -> Unit)? = null
    }

    abstract fun getAppIconIDs(): ArrayList<Int>

    abstract fun getAppLauncherName(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))
        }

        super.onCreate(savedInstanceState)
        /**
        if (!packageName.startsWith("com.simplemobiletools.", true)) {
            if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {
                val label = "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"
                ConfirmationDialog(this, label, positive = R.string.ok, negative = 0) {
                    launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
                }
            }
        }
        */
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))

            val backgroundColor = if (baseConfig.isUsingSystemTheme) {
                resources.getColor(R.color.you_background_color, theme)
            } else {
                baseConfig.backgroundColor
            }

            updateBackgroundColor(backgroundColor)
        }

        if (showTransparentTop) {
            window.statusBarColor = Color.TRANSPARENT
        } else {
            val color = if (baseConfig.isUsingSystemTheme) {
                resources.getColor(R.color.you_status_bar_color)
            } else {
                getProperStatusBarColor()
            }

            updateActionbarColor(color)
        }

        updateRecentsAppIcon()
        updateNavigationBarColor()
    }

    override fun onDestroy() {
        super.onDestroy()
        funAfterSAFPermission = null
        actionOnPermission = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                hideKeyboard()
                finish()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun attachBaseContext(newBase: Context) {
        if (newBase.baseConfig.useEnglish && !isTiramisuPlus()) {
            super.attachBaseContext(MyContextWrapper(newBase).wrap(newBase, "en"))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    fun updateBackgroundColor(color: Int = baseConfig.backgroundColor) {
        //window.decorView.setBackgroundColor(color)
        return
    }

    fun updateActionbarColor(color: Int = getProperStatusBarColor()) {
        /**
        updateStatusbarColor(color)
        setTaskDescription(ActivityManager.TaskDescription(null, null, color))
        */
        return
    }

    fun updateNavigationBarColor(color: Int = baseConfig.navigationBarColor, isColorPreview: Boolean = false) {
        /**
        if (showTransparentNavigation) {
            return
        }

        if (baseConfig.isUsingSystemTheme && !isColorPreview) {
            val navBarColor = getBottomNavigationBackgroundColor()
            window.navigationBarColor = navBarColor
            if (navBarColor.getContrastColor() == 0xFF333333.toInt()) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            } else {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            }
        } else if (baseConfig.navigationBarColor != INVALID_NAVIGATION_BAR_COLOR) {
            try {
                val colorToUse = if (color == -2) -1 else color
                window.navigationBarColor = colorToUse

                if (isOreoPlus()) {
                    if (colorToUse.getContrastColor() == 0xFF333333.toInt()) {
                        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                    } else {
                        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                    }
                }
            } catch (ignored: Exception) {
            }
        }
        */
        return
    }

    fun updateRecentsAppIcon() {
        /**
        if (baseConfig.isUsingModifiedAppIcon) {
            val appIconIDs = getAppIconIDs()
            val currentkeyColorIndex = getCurrentkeyColorIndex()
            if (appIconIDs.size - 1 < currentkeyColorIndex) {
                return
            }

            val recentsIcon = BitmapFactory.decodeResource(resources, appIconIDs[currentkeyColorIndex])
            val title = getAppLauncherName()
            val color = baseConfig.primaryColor

            val description = ActivityManager.TaskDescription(title, recentsIcon, color)
            setTaskDescription(description)
        }
        */
        return
    }

    fun updateMenuItemColors(menu: Menu?, useCrossAsBack: Boolean = false, baseColor: Int = getProperStatusBarColor(), forceWhiteIcons: Boolean = false) {
        /**
        if (menu == null) {
            return
        }

        var color = baseColor.getContrastColor()
        if (forceWhiteIcons) {
            color = Color.WHITE
        }

        for (i in 0 until menu.size()) {
            try {
                menu.getItem(i)?.icon?.setTint(color)
            } catch (ignored: Exception) {
            }
        }
        */
        return
    }

    fun setupToolbar(
        toolbar: MaterialToolbar,
        toolbarNavigationIcon: NavigationIcon = NavigationIcon.None,
        statusBarColor: Int = getProperStatusBarColor(),
        searchMenuItem: MenuItem? = null
    ) {
        /**
        val contrastColor = statusBarColor.getContrastColor()
        toolbar.setBackgroundColor(statusBarColor)
        toolbar.setTitleTextColor(contrastColor)
        toolbar.overflowIcon = resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, contrastColor)

        if (toolbarNavigationIcon != NavigationIcon.None) {
            val drawableId = if (toolbarNavigationIcon == NavigationIcon.Cross) R.drawable.ic_cross_vector else R.drawable.ic_arrow_left_vector
            toolbar.navigationIcon = resources.getColoredDrawableWithColor(drawableId, contrastColor)
        }

        updateMenuItemColors(toolbar.menu, toolbarNavigationIcon == NavigationIcon.Cross, statusBarColor)
        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            finish()
        }

        // this icon is used at closing search
        toolbar.collapseIcon = resources.getColoredDrawableWithColor(R.drawable.ic_arrow_left_vector, contrastColor)

        searchMenuItem?.actionView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.apply {
            applyColorFilter(contrastColor)
        }

        searchMenuItem?.actionView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
            setTextColor(contrastColor)
            setHintTextColor(contrastColor.adjustAlpha(MEDIUM_ALPHA))
            hint = "${getString(R.string.search)}…"

            if (isQPlus()) {
                textCursorDrawable = null
            }
        }

        // search underline
        searchMenuItem?.actionView?.findViewById<View>(androidx.appcompat.R.id.search_plate)?.apply {
            background.setColorFilter(contrastColor, PorterDuff.Mode.MULTIPLY)
        }
        */
        return
    }

    private fun getCurrentkeyColorIndex(): Int {
        val keyColor = baseConfig.keyColor
        getkeyColors().forEachIndexed { index, color ->
            if (color == keyColor) {
                return index
            }
        }
        return 0
    }

    fun setTranslucentNavigation() {
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }

    fun startAboutActivity(appNameId: Int, licenseMask: Long, versionName: String, faqItems: ArrayList<FAQItem>, showFAQBeforeMail: Boolean) {
        hideKeyboard()
        Intent(applicationContext, AboutActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            putExtra(APP_NAME, getString(appNameId))
            putExtra(APP_LICENSES, licenseMask)
            putExtra(APP_VERSION_NAME, versionName)
            putExtra(APP_FAQ, faqItems)
            putExtra(SHOW_FAQ_BEFORE_MAIL, showFAQBeforeMail)
            startActivity(this)
        }
    }

    fun startCustomizationActivity() {
        /** This is a custom fork of an app anyways.
        if (!packageName.contains("slootelibomelpmis".reversed(), true)) {
            if (baseConfig.appRunCount > 100) {
                val label = "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"
                ConfirmationDialog(this, label, positive = R.string.ok, negative = 0) {
                    launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
                }
                return
            }
        }
        */

        Intent(applicationContext, CustomizationActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            startActivity(this)
        }
    }

    fun handleCustomizeColorsClick() {
        startCustomizationActivity()
    }


    // synchronous return value determines only if we are showing the SAF dialog, callback result tells if the SD or OTG permission has been granted
    fun handleSAFDialog(path: String, callback: (success: Boolean) -> Unit): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingSAFDialog(path) || isShowingOTGDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleSAFDialogSdk30(path: String, callback: (success: Boolean) -> Unit): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingSAFDialogSdk30(path)) {
            funAfterSdk30Action = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleSAFCreateDocumentDialogSdk30(path: String, callback: (success: Boolean) -> Unit): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingSAFCreateDocumentDialogSdk30(path)) {
            funAfterSdk30Action = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleAndroidSAFDialog(path: String, callback: (success: Boolean) -> Unit): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingAndroidSAFDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleOTGPermission(callback: (success: Boolean) -> Unit) {
        hideKeyboard()
        if (baseConfig.OTGTreeUri.isNotEmpty()) {
            callback(true)
            return
        }

        funAfterSAFPermission = callback
        WritePermissionDialog(this, Mode.Otg) {
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                try {
                    startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                    return@apply
                } catch (e: Exception) {
                    type = "*/*"
                }

                try {
                    startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    toast(R.string.unknown_error_occurred)
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun deleteSDK30Uris(uris: List<Uri>, callback: (success: Boolean) -> Unit) {
        hideKeyboard()
        if (isRPlus()) {
            funAfterSdk30Action = callback
            try {
                val deleteRequest = MediaStore.createDeleteRequest(contentResolver, uris).intentSender
                startIntentSenderForResult(deleteRequest, DELETE_FILE_SDK_30_HANDLER, null, 0, 0, 0)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        } else {
            callback(false)
        }
    }

    @SuppressLint("NewApi")
    fun updateSDK30Uris(uris: List<Uri>, callback: (success: Boolean) -> Unit) {
        hideKeyboard()
        if (isRPlus()) {
            funAfterUpdate30File = callback
            try {
                val writeRequest = MediaStore.createWriteRequest(contentResolver, uris).intentSender
                startIntentSenderForResult(writeRequest, UPDATE_FILE_SDK_30_HANDLER, null, 0, 0, 0)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        } else {
            callback(false)
        }
    }

    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(this, arrayOf(getPermissionString(permissionId)), GENERIC_PERM_HANDLER)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == 0)
        }
    }

}
