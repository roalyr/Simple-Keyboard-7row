package com.simplemobiletools.keyboard.services

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.text.InputType.TYPE_CLASS_DATETIME
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_MASK_CLASS
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.view.inputmethod.EditorInfo.IME_MASK_ACTION
import android.view.inputmethod.ExtractedTextRequest
import com.simplemobiletools.keyboard.R
import com.simplemobiletools.keyboard.activities.SettingsActivity
import com.simplemobiletools.keyboard.extensions.config
import com.simplemobiletools.keyboard.helpers.*
import com.simplemobiletools.keyboard.views.MyKeyboardView
import kotlinx.android.synthetic.main.keyboard_view_keyboard.view.*

// based on https://www.androidauthority.com/lets-build-custom-keyboard-android-832362/
class SimpleKeyboardIME : InputMethodService(), MyKeyboardView.OnKeyboardActionListener {
    private var SHIFT_PERM_TOGGLE_SPEED =
        500   // how quickly do we have to doubletap shift to enable permanent caps lock
    private val KEYBOARD_LETTERS = 0
    private val KEYBOARD_LETTERS_SECOND = 1

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
        keyboardView = keyboardHolder.keyboard_view as MyKeyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setKeyboardHolder(keyboardHolder.keyboard_holder)
        keyboardView!!.setEditorInfo(currentInputEditorInfo)
        keyboardView!!.mOnKeyboardActionListener = this
        return keyboardHolder!!
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

        when (code) {
            MyKeyboard.KEYCODE_DELETE -> {
                if ((keyboard ?: return).mShiftState == SHIFT_ON_ONE_CHAR) {
                    (keyboard ?: return).mShiftState = SHIFT_OFF
                }

                if ((keyboard ?: return).mControlState == CONTROL_ON_ONE_CHAR) {
                    (keyboard ?: return).mControlState = CONTROL_OFF
                }

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
            MyKeyboard.KEYCODE_SHIFT -> {
                // Disable ctrl key if shift is pressed.
                if ((keyboard ?: return).mControlState == CONTROL_ON_ONE_CHAR) {
                    (keyboard ?: return).mControlState = CONTROL_OFF
                    (keyboardView ?: return).invalidateAllKeys()
                }

                //if (keyboardMode == KEYBOARD_LETTERS) {
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
                //}
                /** else {
                val keyboardXml = if (keyboardMode == KEYBOARD_LETTERS_SECOND) {
                keyboardMode = KEYBOARD_LETTERS_SECOND_SHIFT
                R.xml.keys_symbols_shift
                } else {
                keyboardMode = KEYBOARD_LETTERS_SECOND
                R.xml.keys_letters_ukrainian
                }
                keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
                keyboardView!!.setKeyboard(keyboard!!)
                }
                 */
                (keyboardView ?: return).invalidateAllKeys()
            }
            MyKeyboard.KEYCODE_CONTROL -> {
                // Set key state and process input further below, when next character is provided.
                when {
                    // Need to figure out why CTRL key is reset after cut / paste.
                    //keyboard!!.mControlState == CONTROL_ON_PERMANENT -> { keyboard!!.mControlState = CONTROL_OFF }
                    //System.currentTimeMillis() - lastControlPressTS < CONTROL_PERM_TOGGLE_SPEED -> { keyboard!!.mControlState = CONTROL_ON_PERMANENT }
                    (keyboard ?: return).mControlState == CONTROL_ON_ONE_CHAR -> (keyboard ?: return).mControlState =
                        CONTROL_OFF
                    (keyboard ?: return).mControlState == CONTROL_OFF -> (keyboard ?: return).mControlState =
                        CONTROL_ON_ONE_CHAR
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
            MyKeyboard.KEYCODE_UP -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_UP
                    )
                )
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
            }
            MyKeyboard.KEYCODE_DOWN -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_DOWN
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN
                    )
                )
            }
            MyKeyboard.KEYCODE_LEFT -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_LEFT
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_DPAD_LEFT
                    )
                )
            }
            MyKeyboard.KEYCODE_RIGHT -> {
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_RIGHT
                    )
                )
                inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_DPAD_RIGHT
                    )
                )
            }
            MyKeyboard.KEYCODE_ENTER -> {

                /**
                val enterResourceId = when (getTextForImeAction()) {
                    EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_search_vector
                    EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_GO -> R.drawable.ic_arrow_right_vector
                    EditorInfo.IME_ACTION_SEND -> R.drawable.ic_send_vector
                    else -> R.drawable.ic_enter_vector
                }
                */

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
            MyKeyboard.KEYCODE_MODE_CHANGE -> {
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
            MyKeyboard.KEYCODE_EMOJI -> {
                keyboardView?.openEmojiPalette()
            }
            MyKeyboard.KEYCODE_CLIPBOARD -> {
                keyboardView?.openClipboardManager()
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
                    if ((keyboard ?: return).mControlState == CONTROL_ON_ONE_CHAR) {
                        (keyboard ?: return).mControlState = CONTROL_OFF
                    }
                    inputConnection.clearMetaKeyStates(KeyEvent.META_CTRL_MASK)
                    (keyboardView ?: return).invalidateAllKeys()
                    return
                }

                // Shifting
                if (Character.isLetter(codeChar) && (keyboard ?: return).mShiftState > SHIFT_OFF) {
                    codeChar = Character.toUpperCase(codeChar)
                }

                // If the keyboard is set to symbols and the user presses space, we usually should switch back to the letters keyboard.
                // However, avoid doing that in cases when the EditText for example requires numbers as the input.
                // We can detect that by the text not changing on pressing Space.
                /**
                if (keyboardMode != KEYBOARD_LETTERS && code == MyKeyboard.KEYCODE_SPACE) {
                val originalText = inputConnection.getExtractedText(ExtractedTextRequest(), 0)?.text ?: return
                inputConnection.commitText(codeChar.toString(), 1)
                val newText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
                switchToLetters = originalText != newText
                } else {
                inputConnection.commitText(codeChar.toString(), 1)
                }
                 */
                inputConnection.commitText(codeChar.toString(), 1)

                if ((keyboard ?: return).mShiftState == SHIFT_ON_ONE_CHAR) {
                    (keyboard ?: return).mShiftState = SHIFT_OFF
                    (keyboardView ?: return).invalidateAllKeys()
                }
                if ((keyboard ?: return).mControlState == CONTROL_ON_ONE_CHAR) {
                    (keyboard ?: return).mControlState = CONTROL_OFF
                    (keyboardView ?: return).invalidateAllKeys()
                }
            }
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

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        if (newSelStart == newSelEnd) {
            keyboardView?.closeClipboardManager()
        }
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
            else -> R.xml.keys_letters_english_qwerty
        }
    }
}
