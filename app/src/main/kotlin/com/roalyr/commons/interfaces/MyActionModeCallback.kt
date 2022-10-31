package com.roalyr.commons.interfaces

import android.view.ActionMode

abstract class MyActionModeCallback : ActionMode.Callback {
    var isSelectable: Boolean = false
}
