package com.simplemobiletools.commons.extensions

import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.widget.EditText
import android.widget.TextView
import androidx.core.graphics.ColorUtils

val EditText.value: String get() = text.toString().trim()

fun EditText.onTextChangeListener(onTextChangedAction: (newText: String) -> Unit) = addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        onTextChangedAction(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
})

