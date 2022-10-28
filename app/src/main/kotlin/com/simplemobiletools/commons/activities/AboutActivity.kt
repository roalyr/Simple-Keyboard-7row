package com.simplemobiletools.commons.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.simplemobiletools.keyboard.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseSimpleActivity() {
    private var appName = ""
    private var primaryColor = 0

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0
    private val EASTER_EGG_TIME_LIMIT = 3000L
    private val EASTER_EGG_REQUIRED_CLICKS = 7

    override fun getAppIconIDs(): java.util.ArrayList<Int> = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName(): String = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        primaryColor = getProperPrimaryColor()

        arrayOf(
            about_privacy_policy_icon,
            about_licenses_icon,
            about_version_icon
        ).forEach {
            it.applyColorFilter(textColor)
        }

        arrayOf(about_other_holder).forEach {
            it.background.applyColorFilter(backgroundColor.getContrastColor())
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_nested_scrollview)
        setupToolbar(about_toolbar, NavigationIcon.Arrow)


        setupPrivacyPolicy()
        setupLicense()
        setupVersion()
    }

    private fun setupPrivacyPolicy() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_privacy_policy_holder.beGone()
        }

        about_privacy_policy_holder.setOnClickListener {
            val appId = baseConfig.appId.removeSuffix(".debug").removeSuffix(".pro").removePrefix("com.simplemobiletools.")
            val url = "https://simplemobiletools.com/privacy/$appId.txt"
            launchViewIntent(url)
        }
    }

    private fun setupLicense() {
        if (about_privacy_policy_holder.isGone()) {
            about_licenses_holder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        about_licenses_holder.setOnClickListener {
            Intent(applicationContext, LicenseActivity::class.java).apply {
                putExtra(APP_ICON_IDS, getAppIconIDs())
                putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
                putExtra(APP_LICENSES, intent.getLongExtra(APP_LICENSES, 0))
                startActivity(this)
            }
        }
    }

    private fun setupVersion() {
        var version = intent.getStringExtra(APP_VERSION_NAME) ?: ""
        if (baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
            version += " ${getString(R.string.pro)}"
        }

        val fullVersion = String.format(getString(R.string.version_placeholder, version))
        about_version.text = fullVersion
        about_version_holder.setOnClickListener {
            if (firstVersionClickTS == 0L) {
                firstVersionClickTS = System.currentTimeMillis()
                Handler().postDelayed({
                    firstVersionClickTS = 0L
                    clicksSinceFirstClick = 0
                }, EASTER_EGG_TIME_LIMIT)
            }

            clicksSinceFirstClick++
            if (clicksSinceFirstClick >= EASTER_EGG_REQUIRED_CLICKS) {
                toast(R.string.hello)
                firstVersionClickTS = 0L
                clicksSinceFirstClick = 0
            }
        }
    }
}
