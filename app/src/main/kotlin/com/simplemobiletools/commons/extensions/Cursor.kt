package com.simplemobiletools.commons.extensions

import android.annotation.SuppressLint
import android.database.Cursor

@SuppressLint("Range")
fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getIntValue(key: String) = getInt(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getIntValueOrNull(key: String) = if (isNull(getColumnIndex(key))) null else getInt(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getLongValue(key: String) = getLong(getColumnIndex(key))

