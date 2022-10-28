package com.simplemobiletools.commons.models

data class SharedTheme(
    val textColor: Int,
    val backgroundColor: Int,
    val primaryColor: Int,
    val keyColor: Int,
    val navigationBarColor: Int,
    val lastUpdatedTS: Int = 0,
    val accentColor: Int
)
