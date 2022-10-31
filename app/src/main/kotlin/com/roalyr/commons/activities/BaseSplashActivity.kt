package com.roalyr.commons.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.roalyr.commons.extensions.baseConfig
import com.roalyr.commons.extensions.checkAppSideloading
import com.roalyr.commons.extensions.showSideloadingDialog
import com.roalyr.commons.helpers.SIDELOADING_TRUE
import com.roalyr.commons.helpers.SIDELOADING_UNCHECKED

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
