package com.roalyr.simple_7row_keyboard.activities

import android.content.Intent
import com.roalyr.commons.activities.BaseSplashActivity

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
