package com.roalyr.simple_7row_keyboard.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.util.Xml
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import androidx.annotation.XmlRes
import com.roalyr.simple_7row_keyboard.R
import com.roalyr.simple_7row_keyboard.extensions.config
import kotlin.math.roundToInt

/**
 * Loads an XML description of a keyboard and stores the attributes of the keys. A keyboard consists of rows of keys.
 * @attr ref android.R.styleable#Keyboard_keyWidth
 * @attr ref android.R.styleable#Keyboard_horizontalGap
 */
class MyKeyboard {
    /** Horizontal gap default for all rows  */
    private var mDefaultHorizontalGap = 0

    /** Default key width  */
    private var mDefaultWidth = 0

    /** Default key height  */
    private var mDefaultHeight = 0

    /** Multiplier for the keyboard height */
    var mKeyboardHeightMultiplier: Float = 1F

    /** Is the keyboard in the shifted state  */
    var mShiftState: Int = SHIFT_OFF

    /** If the ctrl key pressed  */
    var mControlState: Int = CONTROL_OFF

    /** If the ctrl key pressed  */
    var mSelectState: Int = SELECT_OFF

    /** Total height of the keyboard, including the padding and keys  */
    var mHeight: Int = 0

    /** Total width of the keyboard, including left side gaps and keys, but not any gaps on the right side. */
    var mMinWidth: Int = 0

    /** List of keys in this keyboard  */
    var mKeys: MutableList<Key?>? = null

    /** Width of the screen available to fit the keyboard  */
    private var mDisplayWidth = 0

    /** What icon should we show at Enter key  */
    private var mEnterKeyType = IME_ACTION_NONE

    /** Keyboard rows  */
    private val mRows = ArrayList<Row?>()

    companion object {
        private const val TAG_KEYBOARD = "Keyboard"
        private const val TAG_ROW = "Row"
        private const val TAG_KEY = "Key"
        private const val EDGE_LEFT = 0x01
        private const val EDGE_RIGHT = 0x02
        const val EDGE_TOP: Int = 1
        const val EDGE_BOTTOM: Int = 2

        const val KEYCODE_SHIFT: Int = -1
        const val KEYCODE_LAYOUT_CHANGE: Int = -2
        const val KEYCODE_TAB: Int = -3
        const val KEYCODE_ENTER: Int = -4
        const val KEYCODE_DELETE: Int = -5
        const val KEYCODE_SPACE: Int = 32
        const val KEYCODE_EMOJI: Int = -6
        const val KEYCODE_CONTROL: Int = -7
        const val KEYCODE_UNDO: Int = -8
        const val KEYCODE_REDO: Int = -9

        const val KEYCODE_UP: Int = -10
        const val KEYCODE_DOWN: Int = -11
        const val KEYCODE_LEFT: Int = -12
        const val KEYCODE_RIGHT: Int = -13

        const val KEYCODE_CLIPBOARD: Int = -14
        const val KEYCODE_SETTINGS: Int = -15
        const val KEYCODE_SEARCH = -16
        const val KEYCODE_LAYOUT_EDIT = -17

        const val KEYCODE_FORWARD_DELETE: Int = -18
        const val KEYCODE_PASTE: Int = -19
        const val KEYCODE_COPY: Int = -20
        const val KEYCODE_CUT: Int = -21
        const val KEYCODE_SELECT: Int = -22

        const val KEYCODE_PGDN: Int = -23
        const val KEYCODE_PGUP: Int = -24
        const val KEYCODE_HOME = -25
        const val KEYCODE_END = -26

        const val KEYCODE_WINDOWMANAGERCLOSE = -27

        fun getDimensionOrFraction(a: TypedArray, index: Int, base: Int, defValue: Int): Int {
            val value = a.peekValue(index) ?: return defValue
            return when (value.type) {
                TypedValue.TYPE_DIMENSION -> a.getDimensionPixelOffset(index, defValue)
                TypedValue.TYPE_FRACTION -> a.getFraction(
                    index,
                    base,
                    base,
                    defValue.toFloat()
                ).toInt()
                else -> defValue
            }
        }
    }

    /**
     * Container for keys in the keyboard. All keys in a row are at the same Y-coordinate. Some of the key size defaults can be overridden per row from
     * what the [MyKeyboard] defines.
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     */
    class Row {
        /** Default width of a key in this row.  */
        var defaultWidth: Int = 0

        private var rowHeight: Int = 0

        /** Default height of a key in this row.  */
        var defaultKeyHeight: Int = 0

        /** Default horizontal gap between keys in this row.  */
        var defaultHorizontalGap: Int = 0

        var mKeys: ArrayList<Key> = ArrayList()

        var parent: MyKeyboard

        constructor(parent: MyKeyboard) {
            this.parent = parent
        }

        constructor(res: Resources, parent: MyKeyboard, parser: XmlResourceParser?) {
            this.parent = parent
            val keyboard_array = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard)
            val row_array = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard_Row)
            defaultWidth = getDimensionOrFraction(
                keyboard_array,
                R.styleable.MyKeyboard_keyWidth,
                parent.mDisplayWidth,
                parent.mDefaultWidth
            )

            rowHeight = getDimensionOrFraction(
                row_array,
                R.styleable.MyKeyboard_Row_rowHeight,
                parent.mDisplayWidth,
                parent.mDefaultWidth
            )

            defaultKeyHeight =
                (rowHeight * this.parent.mKeyboardHeightMultiplier).roundToInt()

            defaultHorizontalGap = getDimensionOrFraction(
                keyboard_array,
                R.styleable.MyKeyboard_horizontalGap,
                parent.mDisplayWidth,
                parent.mDefaultHorizontalGap
            )
            keyboard_array.recycle()
        }
    }

    /**
     * Class for describing the position and characteristics of a single key in the keyboard.
     *
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_keyHeight
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     * @attr ref android.R.styleable#Keyboard_Key_codes
     * @attr ref android.R.styleable#Keyboard_Key_keyIcon
     * @attr ref android.R.styleable#Keyboard_Key_keyLabel
     * @attr ref android.R.styleable#Keyboard_Key_isRepeatable
     * @attr ref android.R.styleable#Keyboard_Key_popupKeyboard
     * @attr ref android.R.styleable#Keyboard_Key_popupCharacters
     * @attr ref android.R.styleable#Keyboard_Key_keyEdgeFlags
     */
    class Key(parent: Row) {
        /** Key code that this key generates.  */
        var code: Int = 0

        /** Key label size factor.  */
        var label_size: Float = 1.0F

        /** Label to display  */
        var label: CharSequence = ""

        /** Whether key should be displayed with different color  */
        var speckey: Boolean = false

        /** Whether key should be made on two rows  */
        var compound: Boolean = false

        /** First row of letters can also be used for inserting numbers by long pressing them, show those numbers  */
        var keyLabelSmall: String = ""

        /** Icon to display instead of a label. Icon takes precedence over a label  */
        var icon: Drawable? = null

        /** Width of the key, not including the gap  */
        var width: Int

        /** Height of the key, not including the gap  */
        var height: Int

        /** The horizontal gap before this key  */
        var gap: Int

        /** X coordinate of the key in the keyboard layout  */
        var x: Int = 0

        /** Y coordinate of the key in the keyboard layout  */
        var y: Int = 0

        /** The current pressed state of this key  */
        var pressed: Boolean = false

        /** Focused state, used after long pressing a key and swiping to alternative keys  */
        var focused: Boolean = false

        /** Popup characters showing after long pressing the key  */
        var popupCharacters: CharSequence? = null

        /**
         * Flags that specify the anchoring to edges of the keyboard for detecting touch events that are just out of the boundary of the key.
         * This is a bit mask of [MyKeyboard.EDGE_LEFT], [MyKeyboard.EDGE_RIGHT].
         */
        private var edgeFlags = 0

        var rowEdgeFlags: Int = 0

        /** The keyboard that this key belongs to  */
        private val keyboard = parent.parent

        /** If this key pops up a mini keyboard, this is the resource id for the XML layout for that keyboard.  */
        var popupResId: Int = 0

        /** Whether this key repeats itself when held down  */
        var repeatable: Boolean = false

        /** Create a key with the given top-left coordinate and extract its attributes from the XML parser.
         * @param res resources associated with the caller's context
         * @param parent the row that this key belongs to. The row must already be attached to a [MyKeyboard].
         * @param x the x coordinate of the top-left
         * @param y the y coordinate of the top-left
         * @param parser the XML parser containing the attributes for this key
         */
        constructor(res: Resources, parent: Row, x: Int, y: Int, parser: XmlResourceParser?) : this(
            parent
        ) {
            this.x = x
            this.y = y
            var a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard)
            width = getDimensionOrFraction(
                a,
                R.styleable.MyKeyboard_keyWidth,
                keyboard.mDisplayWidth,
                parent.defaultWidth
            )
            height = parent.defaultKeyHeight
            gap = getDimensionOrFraction(
                a,
                R.styleable.MyKeyboard_horizontalGap,
                keyboard.mDisplayWidth,
                parent.defaultHorizontalGap
            )
            this.x += gap

            a.recycle()
            a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard_Key)
            code = a.getInt(R.styleable.MyKeyboard_Key_code, 0)
            label_size = a.getFloat(R.styleable.MyKeyboard_Key_labelSize, 1.0F)
            popupCharacters = a.getText(R.styleable.MyKeyboard_Key_popupCharacters)

            popupResId = a.getResourceId(R.styleable.MyKeyboard_Key_popupKeyboard, 0)

            repeatable = a.getBoolean(R.styleable.MyKeyboard_Key_isRepeatable, false)
            speckey = a.getBoolean(R.styleable.MyKeyboard_Key_specKey, false)
            compound = a.getBoolean(R.styleable.MyKeyboard_Key_isCompound, false)


            edgeFlags = a.getInt(R.styleable.MyKeyboard_Key_keyEdgeFlags, 0)
            rowEdgeFlags = a.getInt(R.styleable.MyKeyboard_Key_keyRowEdgeFlags, 0)

            icon = a.getDrawable(R.styleable.MyKeyboard_Key_keyIcon)
            icon?.setBounds(0, 0, icon!!.intrinsicWidth, icon!!.intrinsicHeight)

            label = a.getText(R.styleable.MyKeyboard_Key_keyLabel) ?: ""
            keyLabelSmall = a.getString(R.styleable.MyKeyboard_Key_keyLabelSmall) ?: ""

            // Exclude speckey labels from being printed.
            if (label.isNotEmpty() && !speckey) {
                code = label[0].code
            }
            a.recycle()
        }

        /** Create an empty key with no attributes.  */
        init {
            height = parent.defaultKeyHeight
            width = parent.defaultWidth
            gap = parent.defaultHorizontalGap
        }

        /**
         * Detects if a point falls inside this key.
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return whether or not the point falls inside the key. If the key is attached to an edge, it will assume that all points between the key and
         * the edge are considered to be inside the key.
         */
        fun isInside(x: Int, y: Int): Boolean {
            val leftEdge = edgeFlags and EDGE_LEFT > 0
            val rightEdge = edgeFlags and EDGE_RIGHT > 0
            return ((x >= this.x || leftEdge && x <= this.x + width)
                && (x < this.x + width || rightEdge && x >= this.x)
                && (y >= this.y && y <= this.y + height)
                && (y < this.y + height && y >= this.y))
        }
    }

    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows that have a keyboard mode defined but don't match the specified mode.
     * @param context the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param enterKeyType determines what icon should we show on Enter key
     */
    @JvmOverloads
    constructor(context: Context, @XmlRes xmlLayoutResId: Int, enterKeyType: Int) {
        mDisplayWidth = 500
        mDefaultHorizontalGap = 0
        mDefaultWidth = mDisplayWidth / MAX_KEYS_PER_MINI_ROW
        mDefaultHeight = 500
        mKeyboardHeightMultiplier =
            getKeyboardHeightMultiplier(context.config.keyboardHeightMultiplier)
        mKeys = ArrayList()
        mEnterKeyType = enterKeyType
        loadKeyboard(context, context.resources.getXml(xmlLayoutResId))
    }

    /**
     * Creates a blank keyboard from the given resource file and populates it with the specified characters in left-to-right, top-to-bottom fashion,
     * using the specified number of columns. If the specified number of columns is -1, then the keyboard will fit as many keys as possible in each row.
     * @param context the application or service context
     * @param layoutTemplateResId the layout template file, containing no keys.
     * @param characters the list of characters to display on the keyboard. One key will be created for each character.
     * @param keyWidth the width of the popup key, make sure it is the same as the key itself
     */
    constructor(
        context: Context,
        layoutTemplateResId: Int,
        characters: CharSequence,
        keyWidth: Int
    ) :
        this(context, layoutTemplateResId, 0) {
        var x = 0
        var y = 0
        var column = 0
        mMinWidth = 0
        val row = Row(this)
        row.defaultKeyHeight = mDefaultHeight
        row.defaultWidth = keyWidth
        row.defaultHorizontalGap = mDefaultHorizontalGap
        mKeyboardHeightMultiplier =
            getKeyboardHeightMultiplier(context.config.keyboardHeightMultiplier)

        // https://stackoverflow.com/a/54198712
        var values = characters

        // Strip an occasional whitespace or comma.
        while ( values.endsWith(",") || values.endsWith(" ") ) {
            values = values.substring(0, values.length - 1)
        }

        // Trim or put blank key.
        val lstValues: List<String> = values.split(",").map {
            (if (it.isNotEmpty()) {
                it.trim()
            } else {
                ','
            }).toString()
        }

        // Process.
        lstValues.forEachIndexed { index, charseq ->
            val key = Key(row)
            key.width = mDefaultWidth
            if (column >= MAX_KEYS_PER_MINI_ROW) {
                column = 0
                x = 0
                y += mDefaultHeight
                mRows.add(row)
                row.mKeys.clear()
            }

            key.x = x
            key.y = y

            // spec[cfg, 0]
            if (charseq.startsWith("spec[")){

                // Apply the regular expression on the string
                val reg = Regex("""spec\[([^{}]*)\;([^{}]*)\]""")
                val results = reg.find(charseq)

                results?.groupValues?.let { groups ->
                    // If results and groupValues aren't null, we've got our values!
                    key.label = groups[1].replace(" ", "")
                    key.code = groups[2].replace(" ", "").toInt()
                }
            } else {
                //if character is non-spec.
                key.label = charseq
                key.code = charseq[0].code
            }

            column++
            x += key.width + key.gap
            (mKeys ?: return@forEachIndexed).add(key)
            row.mKeys.add(key)
            if (x > mMinWidth) {
                mMinWidth = x
            }
        }

        mHeight = y + mDefaultHeight
        mRows.add(row)
    }

    fun setShifted(shiftState: Int): Boolean {
        if (this.mShiftState != shiftState) {
            this.mShiftState = shiftState
            return true
        }

        return false
    }

    private fun createRowFromXml(res: Resources, parser: XmlResourceParser?): Row {
        return Row(res, this, parser)
    }

    private fun createKeyFromXml(
        res: Resources,
        parent: Row,
        x: Int,
        y: Int,
        parser: XmlResourceParser?
    ): Key {
        return Key(res, parent, x, y, parser)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadKeyboard(context: Context, parser: XmlResourceParser) {
        var inKey = false
        var inRow = false
        var row = 0
        var x = 0
        var y = 0
        var key: Key? = null
        var currentRow: Row? = null
        val res = context.resources
        try {
            var event: Int
            while (parser.next().also { event = it } != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    when (parser.name) {
                        TAG_ROW -> {
                            inRow = true
                            x = 0
                            currentRow = createRowFromXml(res, parser)
                            mRows.add(currentRow)
                        }
                        TAG_KEY -> {
                            inKey = true
                            key = createKeyFromXml(res, currentRow ?: return, x, y, parser)
                            (mKeys ?: return).add(key)

                            // Disable Enter key icon swap.
                            /**
                            if (key.code == KEYCODE_ENTER) {
                            val enterResourceId = when (mEnterKeyType) {
                            EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_search_vector
                            EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_GO -> R.drawable.ic_arrow_right_vector
                            EditorInfo.IME_ACTION_SEND -> R.drawable.ic_send_vector
                            else -> R.drawable.ic_enter_vector
                            }
                            key.icon = context.resources.getDrawable(enterResourceId, context.theme)
                            }
                             */

                            currentRow.mKeys.add(key)
                        }
                        TAG_KEYBOARD -> {
                            parseKeyboardAttributes(res, parser)
                        }
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false
                        x += (key ?: return).gap + key.width
                        if (x > mMinWidth) {
                            mMinWidth = x
                        }
                    } else if (inRow) {
                        inRow = false
                        y += (currentRow ?: return).defaultKeyHeight
                        row++
                    }
                }
            }
        } catch (_: Exception) {
        }
        mHeight = y
    }

    private fun parseKeyboardAttributes(res: Resources, parser: XmlResourceParser) {
        val a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.MyKeyboard)
        mDefaultWidth = getDimensionOrFraction(
            a,
            R.styleable.MyKeyboard_keyWidth,
            mDisplayWidth,
            mDisplayWidth / 10
        )
        mDefaultHeight = res.getDimension(R.dimen.key_height).toInt()
        mDefaultHorizontalGap =
            getDimensionOrFraction(a, R.styleable.MyKeyboard_horizontalGap, mDisplayWidth, 0)
        a.recycle()
    }

    private fun getKeyboardHeightMultiplier(multiplierType: Int): Float {
        return when (multiplierType) {
            KEYBOARD_HEIGHT_MULTIPLIER_SMALL -> 0.7F
            KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM -> 0.85F
            KEYBOARD_HEIGHT_MULTIPLIER_LARGE -> 1.0F
            else -> 1.0F
        }
    }
}
