package com.roalyr.commons.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

fun Drawable.applyColorFilter(color: Int): Unit = mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)

