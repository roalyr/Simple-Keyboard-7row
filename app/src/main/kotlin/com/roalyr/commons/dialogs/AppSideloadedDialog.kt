package com.roalyr.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import com.roalyr.simple_7row_keyboard.R
import com.roalyr.commons.extensions.getAlertDialogBuilder
import com.roalyr.commons.extensions.getStringsPackageName
import com.roalyr.commons.extensions.launchViewIntent
import com.roalyr.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_textview.view.*

class AppSideloadedDialog(val activity: Activity, val callback: () -> Unit) {
    private var dialog: AlertDialog? = null
    private val url = "https://play.google.com/store/apps/details?id=${activity.getStringsPackageName()}"

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_textview, null).apply {
            val text = String.format(activity.getString(R.string.sideloaded_app), url)
            text_view.text = Html.fromHtml(text)
            text_view.movementMethod = LinkMovementMethod.getInstance()
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel) { dialog, which -> negativePressed() }
            .setPositiveButton(R.string.download, null)
            .setOnCancelListener { negativePressed() }
            .apply {
                activity.setupDialogStuff(view, this, R.string.app_corrupt) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        downloadApp()
                    }
                }
            }
    }

    private fun downloadApp() {
        activity.launchViewIntent(url)
    }

    private fun negativePressed() {
        dialog?.dismiss()
        callback()
    }
}
