package com.roalyr.simple_7row_keyboard.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.text.InputType.TYPE_CLASS_DATETIME
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_MASK_CLASS
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.view.inputmethod.EditorInfo.IME_MASK_ACTION
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import com.roalyr.simple_7row_keyboard.R
import com.roalyr.simple_7row_keyboard.activities.MainActivity
import com.roalyr.simple_7row_keyboard.activities.SettingsActivity
import com.roalyr.simple_7row_keyboard.extensions.config
import com.roalyr.simple_7row_keyboard.helpers.*
import com.roalyr.simple_7row_keyboard.views.MyKeyboardView
import kotlinx.android.synthetic.main.keyboard_view_keyboard.view.*

// based on https://www.androidauthority.com/lets-build-custom-keyboard-android-832362/
class SimpleKeyboardIME : InputMethodService(), MyKeyboardView.OnKeyboardActionListener {
    private var SHIFT_PERM_TOGGLE_SPEED =
        500   // how quickly do we have to doubletap shift to enable permanent caps lock
    private val KEYBOARD_LETTERS = 0
    private val KEYBOARD_LETTERS_SECOND = 1
    private val KEYBOARD_EDIT_MODE = 2

    private var keyboard: MyKeyboard? = null
    private var keyboardView: MyKeyboardView? = null
    private var lastShiftPressTS = 0L
    private var lastControlPressTS = 0L
    private var keyboardMode = KEYBOARD_LETTERS
    private var inputTypeClass = InputType.TYPE_CLASS_TEXT
    private var enterKeyType = IME_ACTION_NONE
    private var switchToLetters = false


    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
    }

    override fun onCreateInputView(): View {
        val keyboardHolder = layoutInflater.inflate(R.layout.keyboard_view_keyboard, null)
        val keyboardHolder_sub = View(this)

        // Set layout parameters for the empty view to have zero height
        val layoutParams = ViewGroup.LayoutParams(
            0,
            0
        )
        keyboardHolder_sub.layoutParams = layoutParams

        keyboardView = keyboardHolder.keyboard_view as MyKeyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setKeyboardHolder(keyboardHolder.keyboard_holder)
        keyboardView!!.setEditorInfo(currentInputEditorInfo)
        keyboardView!!.mOnKeyboardActionListener = this

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // This ensures it's above other apps
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,    // It should not receive touch events
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.RIGHT // This sets the view at the top of the screen
        params.x = 0 // Adjust X position as needed
        params.y = 0 // Adjust Y position as needed

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(keyboardHolder, params)
        keyboardView!!.mWindowManager = windowManager

        // Hide the keyboardHolder_sub by setting its visibility to GONE
        keyboardHolder_sub.visibility = View.GONE

        return keyboardHolder_sub!!
    }


    private fun View?.removeSelf() {
        this ?: return
        val parentView = parent as? ViewGroup ?: return
        parentView.removeView(this)
    }

    override fun onPress(primaryCode: Int) {
        if (primaryCode != 0) {
            keyboardView?.vibrateIfNeeded()
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        inputTypeClass = (attribute ?: return).inputType and TYPE_MASK_CLASS
        enterKeyType = attribute.imeOptions and (IME_MASK_ACTION or IME_FLAG_NO_ENTER_ACTION)

        val keyboardXml = when (inputTypeClass) {
            TYPE_CLASS_NUMBER, TYPE_CLASS_DATETIME, TYPE_CLASS_PHONE -> {
                // Temporary solution.
                keyboardMode = KEYBOARD_LETTERS_SECOND
                getKeyboardLayoutXML()
            }
            else -> {
                keyboardMode = KEYBOARD_LETTERS
                getKeyboardLayoutXML()
            }
        }

        keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard ?: return)
        keyboardView?.setEditorInfo(attribute)

        // Disable auto capitalization.
        //updateShiftKeyState()

    }


    override fun onKey(code: Int) {
        val inputConnection = currentInputConnection
        if (keyboard == null || inputConnection == null) {
            return
        }

        if (code != MyKeyboard.KEYCODE_SHIFT) {
            lastShiftPressTS = 0
        }

        if (code != MyKeyboard.KEYCODE_CONTROL) {
            lastControlPressTS = 0
        }

        // Reset select key in all instances except....
        if (code != MyKeyboard.KEYCODE_SELECT && code != MyKeyboard.KEYCODE_UP && code != MyKeyboard.KEYCODE_DOWN
            && code != MyKeyboard.KEYCODE_LEFT && code != MyKeyboard.KEYCODE_RIGHT && code != MyKeyboard.KEYCODE_PGDN
            && code != MyKeyboard.KEYCODE_PGUP && code != MyKeyboard.KEYCODE_HOME && code != MyKeyboard.KEYCODE_END) {
            invalidateSelectKey()
        }

        // Reset control key.
        if (code == MyKeyboard.KEYCODE_DELETE || code == MyKeyboard.KEYCODE_FORWARD_DELETE || code == MyKeyboard.KEYCODE_SHIFT) {
            invalidateControlKey()
        }

        // Reset shift key.
        if (code == MyKeyboard.KEYCODE_DELETE || code == MyKeyboard.KEYCODE_FORWARD_DELETE || code == MyKeyboard.KEYCODE_CONTROL) {
            invalidateShiftKey()
        }

        when (code) {
            MyKeyboard.KEYCODE_DELETE -> {
                val selectedText = inputConnection.getSelectedText(0)
                if (TextUtils.isEmpty(selectedText)) {
                    inputConnection.sendKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DEL
                        )
                    )
                    inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                } else {
                    inputConnection.commitText("", 1)
                }
                (keyboardView ?: return).invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_FORWARD_DELETE -> {
                val selectedText = inputConnection.getSelectedText(0)
                if (TextUtils.isEmpty(selectedText)) {
                    inputConnection.sendKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_FORWARD_DEL
                        )
                    )
                    inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_FORWARD_DEL))
                } else {
                    inputConnection.commitText("", 1)
                }
                (keyboardView ?: return).invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_SHIFT -> {
                when {
                    (keyboard ?: return).mShiftState == SHIFT_ON_PERMANENT -> (keyboard ?: return).mShiftState =
                        SHIFT_OFF
                    System.currentTimeMillis() - lastShiftPressTS < SHIFT_PERM_TOGGLE_SPEED -> (keyboard ?: return).mShiftState =
                        SHIFT_ON_PERMANENT
                    (keyboard ?: return).mShiftState == SHIFT_ON_ONE_CHAR -> (keyboard ?: return).mShiftState =
                        SHIFT_OFF
                    (keyboard ?: return).mShiftState == SHIFT_OFF -> (keyboard ?: return).mShiftState =
                        SHIFT_ON_ONE_CHAR
                }

                lastShiftPressTS = System.currentTimeMillis()
                (keyboardView ?: return).invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_CONTROL -> {
                // Set key state and process input further below, when next character is provided.
                when {
                    (keyboard ?: return).mControlState == CONTROL_ON -> (keyboard ?: return).mControlState =
                        CONTROL_OFF
                    (keyboard ?: return).mControlState == CONTROL_OFF -> (keyboard ?: return).mControlState =
                        CONTROL_ON
                }

                lastControlPressTS = System.currentTimeMillis()
                (keyboardView ?: return).invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_TAB -> {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB))
            }
            MyKeyboard.KEYCODE_UNDO -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_Z,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_Z,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_MASK)
            }
            MyKeyboard.KEYCODE_REDO -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_Y,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_Y,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_MASK)
            }
            MyKeyboard.KEYCODE_PASTE -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_V,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_V,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_MASK)
            }
            MyKeyboard.KEYCODE_COPY -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_C,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_C,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_MASK)
            }
            MyKeyboard.KEYCODE_CUT -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_X,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        0,
                        0,
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_X,
                        0,
                        KeyEvent.META_CTRL_MASK
                    )
                )
                inputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_MASK)
            }
            MyKeyboard.KEYCODE_SELECT -> {
                // Set key state and process input further below, when next character is provided.
                when {
                    (keyboard ?: return).mSelectState == SELECT_ON -> (keyboard ?: return).mSelectState =
                        SELECT_OFF
                    (keyboard ?: return).mSelectState == SELECT_OFF -> (keyboard ?: return).mSelectState =
                        SELECT_ON
                }
                (keyboardView ?: return).invalidateAllKeys()
            }


            /////////////////////////////////////////// NAVIGATION AND SELECT KEYS ///////////////////////////////////////
            MyKeyboard.KEYCODE_UP -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_DPAD_UP)
            }

            MyKeyboard.KEYCODE_DOWN -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_DPAD_DOWN)
            }

            MyKeyboard.KEYCODE_LEFT -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_DPAD_LEFT)
            }

            MyKeyboard.KEYCODE_RIGHT -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_DPAD_RIGHT)
            }

            MyKeyboard.KEYCODE_PGUP -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_PAGE_UP)
            }

            MyKeyboard.KEYCODE_PGDN -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_PAGE_DOWN)
            }

            MyKeyboard.KEYCODE_HOME -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_MOVE_HOME)
            }

            MyKeyboard.KEYCODE_END -> {
                processNavigationKey(inputConnection, KeyEvent.KEYCODE_MOVE_END)
            }

            /////////////////////////////////////////// ///////////////////// ///////////////////////////////////////


            MyKeyboard.KEYCODE_ENTER -> {
                val imeOptionsActionId = getImeOptionsActionId()
                if (imeOptionsActionId != IME_ACTION_NONE) {
                    inputConnection.performEditorAction(imeOptionsActionId)
                } else {
                    // Use shift+Enter instead.
                    inputConnection.sendKeyEvent(
                        KeyEvent(
                            0,
                            0,
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_ENTER,
                            0,
                            KeyEvent.META_SHIFT_MASK
                        )
                    )
                    inputConnection.sendKeyEvent(
                        KeyEvent(
                            0,
                            0,
                            KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_ENTER,
                            0,
                            KeyEvent.META_SHIFT_MASK
                        )
                    )
                    inputConnection.clearMetaKeyStates(KeyEvent.META_SHIFT_MASK)
                }
            }
            MyKeyboard.KEYCODE_SEARCH -> {
                // Use shift+Enter instead.
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_SEARCH,
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_SEARCH,
                    )
                )
            }
            MyKeyboard.KEYCODE_LAYOUT_CHANGE -> {
                // TODO: add proper layout cycling
                val keyboardXml = if (keyboardMode == KEYBOARD_LETTERS) {
                    keyboardMode = KEYBOARD_LETTERS_SECOND
                    baseContext.config.keyboardLanguage = LANGUAGE_UKRAINIAN
                    getKeyboardLayoutXML()
                } else {
                    keyboardMode = KEYBOARD_LETTERS
                    baseContext.config.keyboardLanguage = LANGUAGE_ENGLISH_QWERTY
                    getKeyboardLayoutXML()
                }
                keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
                (keyboardView ?: return).setKeyboard(keyboard ?: return)
            }
            MyKeyboard.KEYCODE_LAYOUT_EDIT -> {
                // TODO: add proper layout cycling
                val keyboardXml = if (keyboardMode == KEYBOARD_LETTERS || keyboardMode == KEYBOARD_LETTERS_SECOND) {
                    keyboardMode = KEYBOARD_EDIT_MODE
                    baseContext.config.keyboardLanguage = EDIT_MODE
                    getKeyboardLayoutXML()

                } else {
                    keyboardMode = KEYBOARD_LETTERS
                    // TODO: make return to previous layout.
                    baseContext.config.keyboardLanguage = LANGUAGE_ENGLISH_QWERTY
                    getKeyboardLayoutXML()
                }
                keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
                (keyboardView ?: return).setKeyboard(keyboard ?: return)
            }
            MyKeyboard.KEYCODE_EMOJI -> {
                keyboardView?.openEmojiPalette()
            }
            MyKeyboard.KEYCODE_CLIPBOARD -> {
                keyboardView?.openClipboardManager()
            }
            MyKeyboard.KEYCODE_WINDOWMANAGERCLOSE -> {
                System.exit(0)
            }
            MyKeyboard.KEYCODE_SETTINGS -> {

                Intent(baseContext, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    baseContext.startActivity(this)
                }
            }
            else -> {
                var codeChar = code.toChar()

                // Process ctrl key combination here.
                if ((keyboard ?: return).mControlState > CONTROL_OFF) {

                    // Need to get the keycode integer from the key pressed.
                    val keyeventcode: Int = when (codeChar) {
                        'a' -> KeyEvent.KEYCODE_A
                        'b' -> KeyEvent.KEYCODE_B
                        'c' -> KeyEvent.KEYCODE_C
                        'd' -> KeyEvent.KEYCODE_D
                        'e' -> KeyEvent.KEYCODE_E
                        'f' -> KeyEvent.KEYCODE_F
                        'g' -> KeyEvent.KEYCODE_G
                        'h' -> KeyEvent.KEYCODE_H
                        'i' -> KeyEvent.KEYCODE_I
                        'j' -> KeyEvent.KEYCODE_J
                        'k' -> KeyEvent.KEYCODE_K
                        'l' -> KeyEvent.KEYCODE_L
                        'm' -> KeyEvent.KEYCODE_M
                        'n' -> KeyEvent.KEYCODE_N
                        'o' -> KeyEvent.KEYCODE_O
                        'p' -> KeyEvent.KEYCODE_P
                        'q' -> KeyEvent.KEYCODE_Q
                        'r' -> KeyEvent.KEYCODE_R
                        's' -> KeyEvent.KEYCODE_S
                        't' -> KeyEvent.KEYCODE_T
                        'u' -> KeyEvent.KEYCODE_U
                        'v' -> KeyEvent.KEYCODE_V
                        'w' -> KeyEvent.KEYCODE_W
                        'x' -> KeyEvent.KEYCODE_X
                        'y' -> KeyEvent.KEYCODE_Y
                        'z' -> KeyEvent.KEYCODE_Z
                        else -> 0
                    }
                    inputConnection.sendKeyEvent(
                        KeyEvent(
                            0,
                            0,
                            KeyEvent.ACTION_DOWN,
                            keyeventcode,
                            0,
                            KeyEvent.META_CTRL_MASK
                        )
                    )
                    inputConnection.sendKeyEvent(
                        KeyEvent(
                            0,
                            0,
                            KeyEvent.ACTION_UP,
                            keyeventcode,
                            0,
                            KeyEvent.META_CTRL_MASK
                        )
                    )
                    inputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_MASK)
                    invalidateControlKey()
                    return
                }

                // Shifting
                if (Character.isLetter(codeChar) && (keyboard ?: return).mShiftState > SHIFT_OFF) {
                    codeChar = Character.toUpperCase(codeChar)
                }

                inputConnection.commitText(codeChar.toString(), 1)

                invalidateShiftKey()
                invalidateControlKey()
                invalidateSelectKey()
            }
        }
    }

    private fun processNavigationKey(inputConnection: InputConnection, event: Int) {
        // Select if select is on.
        if ((keyboard ?: return).mSelectState > SELECT_OFF) {
            inputConnection.sendKeyEvent(
                KeyEvent(
                    0,
                    0,
                    KeyEvent.ACTION_DOWN,
                    event,
                    0,
                    KeyEvent.META_SHIFT_MASK
                )
            )
            inputConnection.sendKeyEvent(
                KeyEvent(
                    0,
                    0,
                    KeyEvent.ACTION_UP,
                    event,
                    0,
                    KeyEvent.META_SHIFT_MASK
                )
            )
            inputConnection.clearMetaKeyStates(KeyEvent.META_SHIFT_MASK)
        } else {
            inputConnection.sendKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    event
                )
            )
            inputConnection.sendKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    event
                )
            )
        }
    }

    private fun invalidateShiftKey() {
        if ((keyboard ?: return).mShiftState == SHIFT_ON_ONE_CHAR) {
            (keyboard ?: return).mShiftState = SHIFT_OFF
        }
        (keyboardView ?: return).invalidateAllKeys()
    }

    private fun invalidateControlKey() {
        (keyboard ?: return).mControlState = CONTROL_OFF
        (keyboardView ?: return).invalidateAllKeys()
    }

    private fun invalidateSelectKey() {
        (keyboard ?: return).mSelectState = SELECT_OFF
        (keyboardView ?: return).invalidateAllKeys()
    }

    private fun getImeOptionsActionId(): Int {
        return if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
        }
    }

    override fun onActionUp() {
        if (switchToLetters) {
            keyboardMode = KEYBOARD_LETTERS
            keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)

            val editorInfo = currentInputEditorInfo

            if (editorInfo != null && editorInfo.inputType != InputType.TYPE_NULL && keyboard?.mShiftState != SHIFT_ON_PERMANENT) {
                if (currentInputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
                    keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
                }
            }

            (keyboardView ?: return).setKeyboard(keyboard ?: return)
            switchToLetters = false
        }
    }

    override fun moveCursorLeft() {
        moveCursor(false)
    }

    override fun moveCursorRight() {
        moveCursor(true)
    }

    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 0)
    }

    private fun moveCursor(moveRight: Boolean) {
        val extractedText =
            currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        var newCursorPosition = extractedText.selectionStart
        newCursorPosition = if (moveRight) {
            newCursorPosition + 1
        } else {
            newCursorPosition - 1
        }

        currentInputConnection?.setSelection(newCursorPosition, newCursorPosition)
    }

    // Temporary disable other layouts.
    private fun getKeyboardLayoutXML(): Int {
        return when (baseContext.config.keyboardLanguage) {
            //LANGUAGE_BENGALI -> R.xml.keys_letters_bengali
            //LANGUAGE_BULGARIAN -> R.xml.keys_letters_bulgarian
            //LANGUAGE_ENGLISH_DVORAK -> R.xml.keys_letters_english_dvorak
            //LANGUAGE_ENGLISH_QWERTZ -> R.xml.keys_letters_english_qwertz
            //LANGUAGE_FRENCH -> R.xml.keys_letters_french
            //LANGUAGE_GERMAN -> R.xml.keys_letters_german
            //LANGUAGE_LITHUANIAN -> R.xml.keys_letters_lithuanian
            //LANGUAGE_ROMANIAN -> R.xml.keys_letters_romanian
            //LANGUAGE_RUSSIAN -> R.xml.keys_letters_russian
            //LANGUAGE_SLOVENIAN -> R.xml.keys_letters_slovenian
            //LANGUAGE_SPANISH -> R.xml.keys_letters_spanish_qwerty
            LANGUAGE_UKRAINIAN -> R.xml.keys_letters_ukrainian
            EDIT_MODE -> R.xml.keys_edit
            else -> R.xml.keys_letters_english_qwerty
        }
    }
}
