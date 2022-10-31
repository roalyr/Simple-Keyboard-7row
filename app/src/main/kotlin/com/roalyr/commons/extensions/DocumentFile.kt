package com.roalyr.commons.extensions

import androidx.documentfile.provider.DocumentFile

fun DocumentFile.getItemSize(countHiddenItems: Boolean): Long {
    return if (isDirectory) {
        getDirectorySize(this, countHiddenItems)
    } else {
        length()
    }
}

private fun getDirectorySize(dir: DocumentFile, countHiddenItems: Boolean): Long {
    var size = 0L
    if (dir.exists()) {
        val files = dir.listFiles()
        for (i in files.indices) {
            val file = files[i]
            if (file.isDirectory) {
                size += getDirectorySize(file, countHiddenItems)
            } else if (!file.name!!.startsWith(".") || countHiddenItems) {
                size += file.length()
            }
        }
    }
    return size
}

