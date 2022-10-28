package com.simplemobiletools.commons.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.SIDELOADING_TRUE
import com.simplemobiletools.commons.helpers.SIDELOADING_UNCHECKED
import com.simplemobiletools.keyboard.R

abstract class BaseSplashActivity : AppCompatActivity() {
    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (baseConfig.appSideloadingStatus == SIDELOADING_UNCHECKED) {
            if (checkAppSideloading()) {
                return
            }
        } else if (baseConfig.appSideloadingStatus == SIDELOADING_TRUE) {
            showSideloadingDialog()
            return
        }

        initActivity()

    }
}
