package com.simplemobiletools.commons.extensions

import android.annotation.SuppressLint
import android.database.Cursor

@SuppressLint("Range")
fun Cursor.getStringValue(key: String): String? = getString(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getLongValue(key: String): Long = getLong(getColumnIndex(key))

