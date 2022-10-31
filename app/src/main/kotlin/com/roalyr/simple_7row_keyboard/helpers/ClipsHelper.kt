package com.roalyr.simple_7row_keyboard.helpers

import android.content.Context
import com.roalyr.simple_7row_keyboard.extensions.clipsDB
import com.roalyr.simple_7row_keyboard.models.Clip

class ClipsHelper(val context: Context) {

    // make sure clips have unique values
    fun insertClip(clip: Clip): Long {
        clip.value = clip.value.trim()
        return if (context.clipsDB.getClipWithValue(clip.value) == null) {
            context.clipsDB.insertOrUpdate(clip)
        } else {
            -1
        }
    }
}
