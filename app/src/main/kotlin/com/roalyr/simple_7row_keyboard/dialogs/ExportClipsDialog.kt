package com.roalyr.simple_7row_keyboard.dialogs

import androidx.appcompat.app.AlertDialog
import com.roalyr.commons.activities.BaseSimpleActivity
import com.roalyr.commons.dialogs.ConfirmationDialog
import com.roalyr.commons.dialogs.FilePickerDialog
import com.roalyr.commons.extensions.*
import com.roalyr.simple_7row_keyboard.R
import com.roalyr.simple_7row_keyboard.extensions.config
import kotlinx.android.synthetic.main.dialog_export_clips.view.*

class ExportClipsDialog(
    val activity: BaseSimpleActivity, path: String, private val hidePath: Boolean, callback: (path: String, filename: String) -> Unit
) {
    init {
        var folder = if (path.isNotEmpty() && activity.getDoesFilePathExist(path)) {
            path
        } else {
            activity.internalStoragePath
        }

        val view = activity.layoutInflater.inflate(R.layout.dialog_export_clips, null).apply {
            export_clips_filename.setText("${activity.getString(R.string.app_launcher_name)}_${getCurrentFormattedDateTime()}")

            if (hidePath) {
                export_clips_path_label.beGone()
                export_clips_path.beGone()
            } else {
                export_clips_path.text = activity.humanizePath(folder)
                export_clips_path.setOnClickListener {
                    FilePickerDialog(activity, folder, false, showFAB = true) {
                        export_clips_path.text = activity.humanizePath(it)
                        folder = it
                    }
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.export_clipboard_items) { alertDialog ->
                    alertDialog.showKeyboard(view.export_clips_filename)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = view.export_clips_filename.value
                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val newPath = "${folder.trimEnd('/')}/$filename"
                        if (!newPath.getFilenameFromPath().isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        activity.config.lastExportedClipsFolder = folder
                        if (!hidePath && activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(activity.getString(R.string.file_already_exists_overwrite), newPath.getFilenameFromPath())
                            ConfirmationDialog(activity, title) {
                                callback(newPath, filename)
                                alertDialog.dismiss()
                            }
                        } else {
                            callback(newPath, filename)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
