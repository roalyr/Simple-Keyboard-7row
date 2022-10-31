package com.simplemobiletools.keyboard.helpers

const val SHIFT_OFF: Int = 0
const val SHIFT_ON_ONE_CHAR: Int = 1
const val SHIFT_ON_PERMANENT: Int = 2

const val CONTROL_OFF: Int = 0
const val CONTROL_ON_ONE_CHAR: Int = 1

// limit the count of alternative characters that show up at long pressing a key
const val MAX_KEYS_PER_MINI_ROW: Int = 10

// shared prefs
const val VIBRATE_ON_KEYPRESS: String = "vibrate_on_keypress"
const val SHOW_POPUP_ON_KEYPRESS: String = "show_popup_on_keypress"
const val LAST_EXPORTED_CLIPS_FOLDER: String = "last_exported_clips_folder"
const val KEYBOARD_LANGUAGE: String = "keyboard_language"
const val HEIGHT_MULTIPLIER: String = "height_multiplier"

// differentiate current and pinned clips at the keyboards' Clipboard section
const val ITEM_SECTION_LABEL: Int = 0
const val ITEM_CLIP: Int = 1

const val LANGUAGE_ENGLISH_QWERTY: Int = 0
const val LANGUAGE_UKRAINIAN: Int = 13

// keyboard height multiplier options
const val KEYBOARD_HEIGHT_MULTIPLIER_SMALL: Int = 1
const val KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM: Int = 2
const val KEYBOARD_HEIGHT_MULTIPLIER_LARGE: Int = 3

const val EMOJI_SPEC_FILE_PATH: String = "media/emoji_spec.txt"
