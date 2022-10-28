package com.simplemobiletools.commons.adapters

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.auth.AuthPromptHost
import androidx.viewpager.widget.PagerAdapter
import com.simplemobiletools.keyboard.R
import com.simplemobiletools.commons.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.commons.helpers.PROTECTION_PATTERN
import com.simplemobiletools.commons.helpers.PROTECTION_PIN
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.interfaces.SecurityTab
import com.simplemobiletools.commons.views.MyScrollView

