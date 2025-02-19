package com.roalyr.simple_7row_keyboard.views

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.Paint.Align
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.TextView
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiCompat.EMOJI_SUPPORTED
import com.roalyr.commons.extensions.*
import com.roalyr.commons.helpers.ensureBackgroundThread
import com.roalyr.commons.helpers.isPiePlus
import com.roalyr.simple_7row_keyboard.R
import com.roalyr.simple_7row_keyboard.activities.ManageClipboardItemsActivity
import com.roalyr.simple_7row_keyboard.adapters.ClipsKeyboardAdapter
import com.roalyr.simple_7row_keyboard.adapters.EmojisAdapter
import com.roalyr.simple_7row_keyboard.extensions.*
import com.roalyr.simple_7row_keyboard.helpers.*
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.EDGE_BOTTOM
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.EDGE_TOP
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_DELETE
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_FORWARD_DELETE
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_EMOJI
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_ENTER
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_LAYOUT_CHANGE
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_SHIFT
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_CONTROL
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_SELECT
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_SPACE
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_UNDO
import com.roalyr.simple_7row_keyboard.helpers.MyKeyboard.Companion.KEYCODE_REDO
import com.roalyr.simple_7row_keyboard.interfaces.RefreshClipsListener
import com.roalyr.simple_7row_keyboard.models.Clip
import com.roalyr.simple_7row_keyboard.models.ClipsSectionLabel
import com.roalyr.simple_7row_keyboard.models.ListItem
import kotlinx.android.synthetic.main.keyboard_popup_keyboard.view.*
import kotlinx.android.synthetic.main.keyboard_view_keyboard.view.*
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
class MyKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleRes: Int = 0
) :
    View(context, attrs, defStyleRes) {

    interface OnKeyboardActionListener {
        /**
         * Called when the user presses a key. This is sent before the [.onKey] is called. For keys that repeat, this is only called once.
         * @param primaryCode the unicode of the key being pressed. If the touch is not on a valid key, the value will be zero.
         */
        fun onPress(primaryCode: Int)

        /**
         * Send a key press to the listener.
         * @param code this is the key that was pressed
         */
        fun onKey(code: Int)

        /**
         * Called when the finger has been lifted after pressing a key
         */
        fun onActionUp()

        /**
         * Called when the user long presses Space and moves to the left
         */
        fun moveCursorLeft()

        /**
         * Called when the user long presses Space and moves to the right
         */
        fun moveCursorRight()

        /**
         * Sends a sequence of characters to the listener.
         * @param text the string to be displayed.
         */
        fun onText(text: String)
    }

    private var mKeyboard: MyKeyboard? = null
    private var mCurrentKeyIndex: Int = NOT_A_KEY

    private var mSpecLabelTextSize = 0
    private var mKeyLabelTextSize = 0

    private var mTextColor = 0
    private var mSmallLabelColor = 0
    private var mBackgroundColor = 0
    private var mPrimaryColor = 0
    private var mKeyColor = 0

    private var mPreviewText: TextView? = null
    private val mPreviewPopup: PopupWindow
    private var mPreviewTextSizeLarge = 0
    private var mPreviewHeight = 0

    private val mCoordinates = IntArray(2)
    private val mPopupKeyboard: PopupWindow
    private var mMiniKeyboardContainer: View? = null
    private var mMiniKeyboard: MyKeyboardView? = null
    private var mMiniKeyboardOnScreen = false
    private var mPopupParent: View
    private var mMiniKeyboardOffsetX = 0
    private var mMiniKeyboardOffsetY = 0
    private val mMiniKeyboardCache: MutableMap<MyKeyboard.Key, View?>
    private var mKeys = ArrayList<MyKeyboard.Key>()
    private var mMiniKeyboardSelectedKeyIndex = -1

    var mOnKeyboardActionListener: OnKeyboardActionListener? = null
    private var mVerticalCorrection = 0
    private var mProximityThreshold = 0
    private var mPopupPreviewX = 0
    private var mPopupPreviewY = 0
    private var mLastX = 0
    private var mLastY = 0

    private val mPaint: Paint
    private var mDownTime = 0L
    private var mLastMoveTime = 0L
    private var mLastKey = 0
    private var mLastCodeX = 0
    private var mLastCodeY = 0
    private var mCurrentKey: Int = NOT_A_KEY
    private var mLastKeyTime = 0L
    private var mCurrentKeyTime = 0L
    private val mKeyIndices = IntArray(12)
    private var mPopupX = 0
    private var mPopupY = 0
    private var mRepeatKeyIndex = NOT_A_KEY
    private var mPopupLayout = 0
    private var mAbortKey = false
    private var mIsLongPressingSpace = false
    private var mLastSpaceMoveX = 0
    private var mPopupMaxMoveDistance = 0f
    private var mkeyLabelSmallSize = 0f
    private var mkeyLabelSmallMarginWidth = 0f
    private var mkeyLabelSmallMarginHeight = 0f
    private val mSpaceMoveThreshold: Int
    private var ignoreTouches = false

    private var mKeyBackground: Drawable? = null

    private var mClipboardManagerHolder: View? = null
    public var mWindowManager: WindowManager? = null
    private var mEmojiPaletteHolder: View? = null
    private var emojiCompatMetadataVersion = 0

    // For multi-tap
    private var mLastTapTime = 0L

    /** Whether the keyboard bitmap needs to be redrawn before it's blitted.  */
    private var mDrawPending = false

    /** The dirty region in the keyboard bitmap  */
    private val mDirtyRect = Rect()

    /** The keyboard bitmap for faster updates  */
    private var mBuffer: Bitmap? = null

    /** Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer.  */
    private var mKeyboardChanged = false

    /** The canvas for the above mutable keyboard bitmap  */
    private var mCanvas: Canvas? = null

    /** The accessibility manager for accessibility support  */
    private val mAccessibilityManager: AccessibilityManager

    private var mHandler: Handler? = null

    private var clipboard_history_list = ArrayList<ListItem>()

    companion object {
        private const val NOT_A_KEY = -1
        private val LONG_PRESSABLE_STATE_SET = intArrayOf(R.attr.state_long_pressable)
        private const val MSG_REMOVE_PREVIEW = 1
        private const val MSG_REPEAT = 2
        private const val MSG_LONGPRESS = 3
        private const val DELAY_AFTER_PREVIEW = 100
        private const val DEBOUNCE_TIME = 70
        private const val REPEAT_INTERVAL = 50 // ~20 keys per second
        private const val REPEAT_START_DELAY = 400
        private const val LONGPRESS_TIMEOUT = 200
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        mPopupLayout = R.layout.keyboard_popup_keyboard
        mKeyBackground = resources.getDrawable(R.drawable.keyboard_key_selector, context.theme)
        mVerticalCorrection = resources.getDimension(R.dimen.vertical_correction).toInt()

        mPreviewHeight = resources.getDimension(R.dimen.key_height).toInt()
        mSpaceMoveThreshold = resources.getDimension(R.dimen.medium_margin).toInt()
        mTextColor = context.getProperTextColor()
        mSmallLabelColor = context.getProperSmalllabelColor()
        mBackgroundColor = context.getProperBackgroundColor()
        mPrimaryColor = context.getProperPrimaryColor()
        mKeyColor = context.getProperKeyColor()

        mPreviewPopup = PopupWindow(context)
        mPreviewText =
            inflater.inflate(resources.getLayout(R.layout.keyboard_key_preview), null) as TextView
        mPreviewTextSizeLarge = context.resources.getDimension(R.dimen.preview_text_size).toInt()
        mPreviewPopup.contentView = mPreviewText
        mPreviewPopup.setBackgroundDrawable(null)

        mPreviewPopup.isTouchable = false
        mPopupKeyboard = PopupWindow(context)
        mPopupKeyboard.setBackgroundDrawable(null)
        mPopupParent = this
        mPaint = Paint()
        mPaint.isAntiAlias = true

        mPaint.textSize = resources.getDimension(R.dimen.key_label_text_size)

        mPaint.textAlign = Align.CENTER
        mPaint.alpha = 255
        mMiniKeyboardCache = HashMap()
        mAccessibilityManager =
            (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager)
        mPopupMaxMoveDistance = resources.getDimension(R.dimen.popup_max_move_distance)

        mSpecLabelTextSize = resources.getDimension(R.dimen.spec_label_text_size).toInt()
        mKeyLabelTextSize = resources.getDimension(R.dimen.key_label_text_size).toInt()

        mkeyLabelSmallSize = resources.getDimension(R.dimen.top_small_label_size)
        mkeyLabelSmallMarginWidth = resources.getDimension(R.dimen.top_small_label_margin_width)
        mkeyLabelSmallMarginHeight = resources.getDimension(R.dimen.top_small_label_margin_height)

    }

    @SuppressLint("HandlerLeak")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mHandler == null) {
            mHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MSG_REMOVE_PREVIEW -> (mPreviewText ?: return).visibility = INVISIBLE
                        MSG_REPEAT -> if (repeatKey(false)) {
                            val repeat = Message.obtain(this, MSG_REPEAT)
                            sendMessageDelayed(repeat, REPEAT_INTERVAL.toLong())
                        }
                        MSG_LONGPRESS -> openPopupIfRequired(msg.obj as MotionEvent)
                    }
                }
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        closeClipboardManager()
        closeEmojiPalette()

        if (visibility == VISIBLE) {
            mTextColor = context.getProperTextColor()
            mSmallLabelColor = context.getProperSmalllabelColor()
            mBackgroundColor = context.getProperBackgroundColor()
            mPrimaryColor = context.getProperPrimaryColor()
            mKeyColor = context.getProperKeyColor()

            val strokeColor = context.getStrokeColor()

            val toolbarColor = if (context.config.isUsingSystemTheme) {
                resources.getColor(R.color.you_keyboard_toolbar_color, context.theme)
            } else {
                mBackgroundColor.darkenColor()
            }

            val darkerColor = if (context.config.isUsingSystemTheme) {
                resources.getColor(R.color.you_keyboard_background_color, context.theme)
            } else {
                mBackgroundColor.darkenColor(2)
            }

            val miniKeyboardBackgroundColor = if (context.config.isUsingSystemTheme) {
                resources.getColor(R.color.you_keyboard_toolbar_color, context.theme)
            } else {
                mBackgroundColor.darkenColor(4)
            }

            if (changedView == mini_keyboard_view) {
                val previewBackground = background as LayerDrawable
                previewBackground.findDrawableByLayerId(R.id.button_background_shape)
                    .applyColorFilter(miniKeyboardBackgroundColor)
                previewBackground.findDrawableByLayerId(R.id.button_background_stroke)
                    .applyColorFilter(strokeColor)
                background = previewBackground
            } else {
                background.applyColorFilter(darkerColor)
            }

            val rippleBg = resources.getDrawable(
                R.drawable.clipboard_background,
                context.theme
            ) as RippleDrawable
            val layerDrawable =
                rippleBg.findDrawableByLayerId(R.id.clipboard_background_holder) as LayerDrawable
            layerDrawable.findDrawableByLayerId(R.id.clipboard_background_stroke)
                .applyColorFilter(strokeColor)
            layerDrawable.findDrawableByLayerId(R.id.clipboard_background_shape)
                .applyColorFilter(mBackgroundColor)

            val wasDarkened = mBackgroundColor != mBackgroundColor.darkenColor()


            mClipboardManagerHolder?.apply {
                clipboard_manager_holder.background = ColorDrawable(toolbarColor)

                clipboard_manager_delete.applyColorFilter(mTextColor)
                clipboard_manager_delete.background = ColorDrawable(mPrimaryColor)

                clipboard_manager_manage.applyColorFilter(mTextColor)
                clipboard_manager_manage.background = ColorDrawable(mPrimaryColor)

                clipboard_manager_close.applyColorFilter(mTextColor)
                clipboard_manager_close.background = ColorDrawable(mPrimaryColor)

                clipboard_manager_undo.applyColorFilter(mTextColor)
                clipboard_manager_undo.background = ColorDrawable(mPrimaryColor)

                clipboard_manager_redo.applyColorFilter(mTextColor)
                clipboard_manager_redo.background = ColorDrawable(mPrimaryColor)

                clipboard_content_placeholder_1.setTextColor(mTextColor)
                clipboard_content_placeholder_2.setTextColor(mTextColor)

            }

            mEmojiPaletteHolder?.apply {
                emoji_palette_holder.background = ColorDrawable(toolbarColor)

                emoji_palette_undo.applyColorFilter(mTextColor)
                emoji_palette_undo.background = ColorDrawable(mPrimaryColor)

                emoji_palette_redo.applyColorFilter(mTextColor)
                emoji_palette_redo.background = ColorDrawable(mPrimaryColor)

                emoji_palette_close.applyColorFilter(mTextColor)
                emoji_palette_close.background = ColorDrawable(mPrimaryColor)

                emoji_palette_delete.applyColorFilter(mTextColor)
                emoji_palette_delete.background = ColorDrawable(mPrimaryColor)

                emoji_palette_delete_forward.applyColorFilter(mTextColor)
                emoji_palette_delete_forward.background = ColorDrawable(mPrimaryColor)

            }

            setupEmojis()
            setupStoredClips()
        }
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the view will re-layout itself to accommodate the keyboard.
     * @param keyboard the keyboard to display in this view
     */
    fun setKeyboard(keyboard: MyKeyboard) {
        if (mKeyboard != null) {
            showPreview(NOT_A_KEY)
        }

        closeClipboardManager()
        removeMessages()
        mKeyboard = keyboard
        val keys = (mKeyboard ?: return).mKeys
        mKeys = (keys ?: return).toMutableList() as ArrayList<MyKeyboard.Key>
        requestLayout()
        mKeyboardChanged = true
        invalidateAllKeys()
        computeProximityThreshold(keyboard)
        mMiniKeyboardCache.clear()
        // Not really necessary to do every time, but will free up views
        // Switching to a different keyboard should abort any pending keys so that the key up
        // doesn't get delivered to the old or new keyboard
        mAbortKey = true // Until the next ACTION_DOWN
    }

    /** Sets the top row above the keyboard containing a couple buttons and the clipboard **/
    fun setKeyboardHolder(keyboardHolder: View) {
        mClipboardManagerHolder = keyboardHolder.clipboard_manager_holder
        mEmojiPaletteHolder = keyboardHolder.emoji_palette_holder

        val clipboardManager =
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        clipboardManager.addPrimaryClipChangedListener {
            val clipboardContent = clipboardManager.primaryClip?.getItemAt(0)?.text?.trim()
            setupStoredClips()
        }

        (mClipboardManagerHolder ?: return).apply {
            clipboard_manager_close.setOnClickListener {
                vibrateIfNeeded()
                closeClipboardManager()
            }

            clipboard_manager_delete.setOnClickListener {
                vibrateIfNeeded()
                clearClipboardContent()
            }

            clipboard_manager_undo.setOnClickListener {
                vibrateIfNeeded()
                (mOnKeyboardActionListener ?: return@setOnClickListener).onKey(KEYCODE_UNDO)
            }

            clipboard_manager_redo.setOnClickListener {
                vibrateIfNeeded()
                (mOnKeyboardActionListener ?: return@setOnClickListener).onKey(KEYCODE_REDO)
            }

            clipboard_manager_manage.setOnClickListener {
                Intent(context, ManageClipboardItemsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(this)
                }
            }
        }

        (mEmojiPaletteHolder ?: return).apply {
            emoji_palette_close.setOnClickListener {
                vibrateIfNeeded()
                closeEmojiPalette()
            }

            emoji_palette_undo.setOnClickListener {
                vibrateIfNeeded()
                (mOnKeyboardActionListener ?: return@setOnClickListener).onKey(KEYCODE_UNDO)
            }

            emoji_palette_redo.setOnClickListener {
                vibrateIfNeeded()
                (mOnKeyboardActionListener ?: return@setOnClickListener).onKey(KEYCODE_REDO)
            }

            emoji_palette_delete.setOnClickListener {
                vibrateIfNeeded()
                (mOnKeyboardActionListener ?: return@setOnClickListener).onKey(KEYCODE_DELETE)
            }

            emoji_palette_delete_forward.setOnClickListener {
                vibrateIfNeeded()
                (mOnKeyboardActionListener ?: return@setOnClickListener).onKey(KEYCODE_FORWARD_DELETE)
            }

        }
    }

    private fun clearClipboardContent() {
        clipboard_history_list.clear()
        val clipboardManager =
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        if (isPiePlus()) {
            clipboardManager.clearPrimaryClip()
        } else {
            val clip = ClipData.newPlainText("", "")
            clipboardManager.setPrimaryClip(clip)
        }
    }

    fun setEditorInfo(editorInfo: EditorInfo) {
        emojiCompatMetadataVersion =
            editorInfo.extras?.getInt(EmojiCompat.EDITOR_INFO_METAVERSION_KEY, 0) ?: 0
    }

    fun vibrateIfNeeded() {
        if (context.config.vibrateOnKeypress) {
            performHapticFeedback()
        }
    }

    private fun setShifted(shiftState: Int) {
        if (mKeyboard?.setShifted(shiftState) == true) {
            invalidateAllKeys()
        }
    }

    /**
     * Returns the state of the shift key of the keyboard, if any.
     * @return true if the shift is in a pressed state, false otherwise
     */
    private fun isShifted(): Boolean {
        return (mKeyboard?.mShiftState ?: SHIFT_OFF) > SHIFT_OFF
    }

    private fun setPopupOffset(x: Int, y: Int) {
        mMiniKeyboardOffsetX = x
        mMiniKeyboardOffsetY = y
        if (mPreviewPopup.isShowing) {
            mPreviewPopup.dismiss()
        }
    }

    private fun adjustCase(label: CharSequence): CharSequence? {
        var newLabel: CharSequence? = label
        if (newLabel != null && newLabel.isNotEmpty() && (mKeyboard ?: return null).mShiftState > SHIFT_OFF && newLabel.length < 3 && Character.isLowerCase(
                newLabel[0]
            )
        ) {
            newLabel = newLabel.toString().uppercase(Locale.getDefault())
        }
        return newLabel
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mKeyboard == null) {
            setMeasuredDimension(0, 0)
        } else {
            var width = (mKeyboard ?: return).mMinWidth
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec)
            }
            setMeasuredDimension(width, (mKeyboard ?: return).mHeight)
        }
    }

    /**
     * Compute the average distance between adjacent keys (horizontally and vertically) and square it to get the proximity threshold. We use a square here and
     * in computing the touch distance from a key's center to avoid taking a square root.
     * @param keyboard
     */
    private fun computeProximityThreshold(keyboard: MyKeyboard?) {
        if (keyboard == null) {
            return
        }

        val keys = mKeys
        val length = keys.size
        var dimensionSum = 0
        for (i in 0 until length) {
            val key = keys[i]
            dimensionSum += min(key.width, key.height) + key.gap
        }

        if (dimensionSum < 0 || length == 0) {
            return
        }

        mProximityThreshold = (dimensionSum * 1.4f / length).toInt()
        mProximityThreshold *= mProximityThreshold // Square it
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            onBufferDraw()
        }
        canvas.drawBitmap(mBuffer ?: return, 0f, 0f, null)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun onBufferDraw() {
        if (mBuffer == null || mKeyboardChanged) {
            if (mBuffer == null || mKeyboardChanged && ((mBuffer ?: return).width != width || (mBuffer ?: return).height != height)) {
                // Make sure our bitmap is at least 1x1
                val width = max(1, width)
                val height = max(1, height)
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                mCanvas = Canvas(mBuffer ?: return)
            }
            invalidateAllKeys()
            mKeyboardChanged = false
        }

        if (mKeyboard == null) {
            return
        }

        (mCanvas ?: return).save()
        val canvas = mCanvas
        (canvas ?: return).clipRect(mDirtyRect)
        val paint = mPaint
        paint.color = mTextColor
        val keys = mKeys

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val keyCount = keys.size
        for (i in 0 until keyCount) {
            val key = keys[i]
            val code = key.code
            val speckey = key.speckey
            val edgeflag = key.rowEdgeFlags
            var keyBackground = mKeyBackground


            /////////////////////////////////////////////// KEY HANDLING ///////////////////////////////
            // Special keys.
            if (speckey && keyBackground != null) {
                keyBackground =
                    resources.getDrawable(R.drawable.keyboard_speckey_background, context.theme)
                keyBackground.applyColorFilter(mPrimaryColor)
            }

            // Ordinary keys
            if (!speckey && keyBackground != null) {
                keyBackground =
                    resources.getDrawable(R.drawable.keyboard_key_background, context.theme)
                keyBackground.applyColorFilter(mKeyColor)
            }

            // If we are using compund key style (2 rows).
            if (key.compound) {
                if (edgeflag == EDGE_TOP) {
                    keyBackground = resources.getDrawable(R.drawable.keyboard_compound_key_background_down, context.theme)
                } else if (edgeflag == EDGE_BOTTOM) {
                    keyBackground = resources.getDrawable(R.drawable.keyboard_compound_key_background_up, context.theme)
                }
                keyBackground?.applyColorFilter(mKeyColor)
            }

            // Switch the character to uppercase if shift is pressed
            var label = adjustCase(key.label)?.toString()
            val bounds = (keyBackground ?: return).bounds
            if (key.width != bounds.right || key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height)
            }

            keyBackground.state = when {
                key.pressed -> intArrayOf(android.R.attr.state_pressed)
                key.focused -> intArrayOf(android.R.attr.state_focused)
                else -> intArrayOf()
            }


            canvas.translate(key.x.toFloat(), key.y.toFloat())
            keyBackground.draw(canvas)


            // For keys with text labels.
            if (label?.isNotEmpty() == true) {

                // Control key lable switching.
                if (code == KEYCODE_CONTROL) {
                    val textlabel = when ((mKeyboard ?: return).mControlState) {
                        CONTROL_OFF -> "Ctrl"
                        CONTROL_ON -> "CTRL"
                        else -> "CTRL"
                    }
                    label = textlabel
                }

                // Select key icon switching.
                if (code == KEYCODE_SELECT) {
                    val textlabel = when ((mKeyboard ?: return).mSelectState) {
                        SELECT_OFF -> "○"
                        else -> "◉"
                    }
                    label = textlabel
                }

                // For characters, use large font. For labels like "Done", use small font.
                if (label.length > 1) {
                    paint.textSize = mSpecLabelTextSize.toFloat()*key.label_size
                    paint.typeface = Typeface.DEFAULT_BOLD
                } else {
                    paint.textSize = mKeyLabelTextSize.toFloat()*key.label_size
                    paint.typeface = Typeface.DEFAULT
                }

                // Draw main label. centered if no small label
                if (key.keyLabelSmall.isEmpty()) {
                    canvas.drawText(
                        label,
                        (key.width / 2).toFloat(),
                        key.height / 2 + (paint.textSize - paint.descent()) / 2,
                        paint
                    )
                } else {
                    canvas.drawText(
                        label,
                        (key.width / 2).toFloat(),
                        key.height / 2 + (paint.textSize - paint.descent()),
                        paint
                    )
                }

                // Draw top small label?
                val smallLetterPaint = Paint()
                // Ordinary key.
                if (!speckey) {
                    smallLetterPaint.apply {
                        set(paint)
                        color = mSmallLabelColor
                        textSize = mkeyLabelSmallSize
                        typeface = Typeface.DEFAULT
                    }
                // Otheerwise use ordinary key text color.
                } else {
                    smallLetterPaint.apply {
                        set(paint)
                        color = mTextColor
                        textSize = mkeyLabelSmallSize
                        typeface = Typeface.DEFAULT
                    }
                }
                if (key.keyLabelSmall.isNotEmpty()) {
                    canvas.drawText(
                        key.keyLabelSmall,
                        key.width - mkeyLabelSmallMarginWidth,
                        mkeyLabelSmallMarginHeight,
                        smallLetterPaint
                    )
                }

                // Turn off drop shadow
                paint.setShadowLayer(0f, 0f, 0f, 0)

                // Only for keys with icons.
            } else if (key.icon != null && mKeyboard != null) {

                // Shift key icon switching.
                if (code == KEYCODE_SHIFT) {
                    val drawableId = when ((mKeyboard ?: return).mShiftState) {
                        SHIFT_OFF -> R.drawable.ic_caps_outline_vector
                        SHIFT_ON_ONE_CHAR -> R.drawable.ic_caps_vector
                        else -> R.drawable.ic_caps_underlined_vector
                    }
                    key.icon = resources.getDrawable(drawableId)
                }


                // This is to make text and icon visible on background
                if (code == KEYCODE_ENTER) {
                    (key.icon ?: return).applyColorFilter(mPrimaryColor.getContrastColor())
                } else if (code == KEYCODE_DELETE || code == KEYCODE_SHIFT || code == KEYCODE_EMOJI) {
                    (key.icon ?: return).applyColorFilter(mTextColor)
                }

                // Icon placement?
                val drawableX = (key.width - (key.icon ?: return).intrinsicWidth) / 2
                val drawableY = (key.height - (key.icon ?: return).intrinsicHeight) / 2
                canvas.translate(drawableX.toFloat(), drawableY.toFloat())
                (key.icon ?: return).setBounds(0, 0, (key.icon ?: return).intrinsicWidth, (key.icon ?: return).intrinsicHeight)
                (key.icon ?: return).draw(canvas)
                canvas.translate(-drawableX.toFloat(), -drawableY.toFloat())
            }
            canvas.translate(-key.x.toFloat(), -key.y.toFloat())
        }

        /**
        // Overlay a dark rectangle to dim the keyboard
        if (mMiniKeyboardOnScreen) {
        paint.color = Color.BLACK.adjustAlpha(0.3f)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
         */

        (mCanvas ?: return).restore()
        mDrawPending = false
        mDirtyRect.setEmpty()
    }

    private fun getPressedKeyIndex(x: Int, y: Int): Int {
        return mKeys.indexOfFirst {
            it.isInside(x, y)
        }
    }

    private fun detectAndSendKey(index: Int, x: Int, y: Int, eventTime: Long) {
        if (index != NOT_A_KEY && index < mKeys.size) {
            val key = mKeys[index]
            getPressedKeyIndex(x, y)
            (mOnKeyboardActionListener ?: return).onKey(key.code)
            mLastTapTime = eventTime
        }
    }

    private fun showPreview(keyIndex: Int) {
        if (!context.config.showPopupOnKeypress) {
            return
        }

        val oldKeyIndex = mCurrentKeyIndex
        val previewPopup = mPreviewPopup
        mCurrentKeyIndex = keyIndex
        // Release the old key and press the new key
        val keys = mKeys
        if (oldKeyIndex != mCurrentKeyIndex) {
            if (oldKeyIndex != NOT_A_KEY && keys.size > oldKeyIndex) {
                val oldKey = keys[oldKeyIndex]
                oldKey.pressed = false
                invalidateKey(oldKeyIndex)
                val keyCode = oldKey.code
                sendAccessibilityEventForUnicodeCharacter(
                    AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED,
                    keyCode
                )
            }

            if (mCurrentKeyIndex != NOT_A_KEY && keys.size > mCurrentKeyIndex) {
                val newKey = keys[mCurrentKeyIndex]

                val code = newKey.code


                // What does this do?
                //if (code == KEYCODE_SHIFT || code == KEYCODE_CONTROL || code == KEYCODE_MODE_CHANGE || code == KEYCODE_DELETE || code == KEYCODE_ENTER || code == KEYCODE_SPACE) {
                //  newKey.pressed = true
                // }

                invalidateKey(mCurrentKeyIndex)
                sendAccessibilityEventForUnicodeCharacter(
                    AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED,
                    code
                )
            }
        }

        // If key changed and preview is on ...
        if (oldKeyIndex != mCurrentKeyIndex) {
            if (previewPopup.isShowing) {
                if (keyIndex == NOT_A_KEY) {
                    (mHandler ?: return).sendMessageDelayed(
                        (mHandler ?: return).obtainMessage(MSG_REMOVE_PREVIEW),
                        DELAY_AFTER_PREVIEW.toLong()
                    )
                }
            }

            if (keyIndex != NOT_A_KEY) {
                showKey(keyIndex)
            }
        }
    }

    private fun showKey(keyIndex: Int) {
        val previewPopup = mPreviewPopup
        val keys = mKeys
        if (keyIndex < 0 || keyIndex >= mKeys.size) {
            return
        }

        val key = keys[keyIndex]
        if (key.icon != null) {
            (mPreviewText ?: return).setCompoundDrawables(null, null, null, key.icon)
        } else {
            if (key.label.length > 1) {
                (mPreviewText ?: return).setTextSize(TypedValue.COMPLEX_UNIT_PX, mKeyLabelTextSize.toFloat())
                (mPreviewText ?: return).typeface = Typeface.DEFAULT_BOLD
            } else {
                (mPreviewText ?: return).setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mPreviewTextSizeLarge.toFloat()
                )
                (mPreviewText ?: return).typeface = Typeface.DEFAULT
            }

            (mPreviewText ?: return).setCompoundDrawables(null, null, null, null)
            try {
                (mPreviewText ?: return).text = adjustCase(key.label)
            } catch (ignored: Exception) {
            }
        }

        val previewBackgroundColor = if (context.config.isUsingSystemTheme) {
            resources.getColor(R.color.you_keyboard_toolbar_color, context.theme)
        } else {
            mBackgroundColor.darkenColor(4)
        }

        val previewBackground = (mPreviewText ?: return).background as LayerDrawable
        previewBackground.findDrawableByLayerId(R.id.button_background_shape)
            .applyColorFilter(previewBackgroundColor)
        previewBackground.findDrawableByLayerId(R.id.button_background_stroke)
            .applyColorFilter(context.getStrokeColor())
        (mPreviewText ?: return).background = previewBackground

        (mPreviewText ?: return).setTextColor(mTextColor)
        (mPreviewText ?: return).measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = max((mPreviewText ?: return).measuredWidth, key.width)
        val popupHeight = mPreviewHeight
        val lp = (mPreviewText ?: return).layoutParams
        lp?.width = popupWidth
        lp?.height = popupHeight

        mPopupPreviewX = key.x
        mPopupPreviewY = key.y - popupHeight

        (mHandler ?: return).removeMessages(MSG_REMOVE_PREVIEW)
        getLocationInWindow(mCoordinates)
        mCoordinates[0] += mMiniKeyboardOffsetX // Offset may be zero
        mCoordinates[1] += mMiniKeyboardOffsetY // Offset may be zero

        // Set the preview background state
        (mPreviewText ?: return).background.state = if (key.popupResId != 0) {
            LONG_PRESSABLE_STATE_SET
        } else {
            EMPTY_STATE_SET
        }

        mPopupPreviewX += mCoordinates[0]
        mPopupPreviewY += mCoordinates[1]

        // If the popup cannot be shown above the key, put it on the side
        getLocationOnScreen(mCoordinates)
        if (mPopupPreviewY + mCoordinates[1] < 0) {
            // If the key you're pressing is on the left side of the keyboard, show the popup on
            // the right, offset by enough to see at least one key to the left/right.
            if (key.x + key.width <= width / 2) {
                mPopupPreviewX += (key.width * 2.5).toInt()
            } else {
                mPopupPreviewX -= (key.width * 2.5).toInt()
            }
            mPopupPreviewY += popupHeight
        }

        previewPopup.dismiss()


        // Add  && !key.speckey to exclude them from popups.
        if (key.label.isNotEmpty()) {
            previewPopup.width = popupWidth
            previewPopup.height = popupHeight
            previewPopup.showAtLocation(
                mPopupParent,
                Gravity.NO_GRAVITY,
                mPopupPreviewX,
                mPopupPreviewY
            )
            (mPreviewText ?: return).visibility = VISIBLE
        }
    }

    private fun sendAccessibilityEventForUnicodeCharacter(eventType: Int, code: Int) {
        if (mAccessibilityManager.isEnabled) {
            val event = AccessibilityEvent.obtain(eventType)
            onInitializeAccessibilityEvent(event)
            val text: String = when (code) {
                KEYCODE_DELETE -> context.getString(R.string.keycode_delete)
                KEYCODE_ENTER -> context.getString(R.string.keycode_enter)
                KEYCODE_LAYOUT_CHANGE -> context.getString(R.string.keycode_mode_change)
                KEYCODE_SHIFT -> context.getString(R.string.keycode_shift)
                KEYCODE_CONTROL -> context.getString(R.string.keycode_control)
                else -> code.toChar().toString()
            }
            event.text.add(text)
            mAccessibilityManager.sendAccessibilityEvent(event)
        }
    }

    /**
     * Requests a redraw of the entire keyboard. Calling [.invalidate] is not sufficient because the keyboard renders the keys to an off-screen buffer and
     * an invalidate() only draws the cached buffer.
     */
    fun invalidateAllKeys() {
        mDirtyRect.union(0, 0, width, height)
        mDrawPending = true
        invalidate()
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only one key is changing it's content. Any changes that
     * affect the position or size of the key may not be honored.
     * @param keyIndex the index of the key in the attached [MyKeyboard].
     */
    private fun invalidateKey(keyIndex: Int) {
        if (keyIndex < 0 || keyIndex >= mKeys.size) {
            return
        }

        val key = mKeys[keyIndex]
        mDirtyRect.union(
            key.x, key.y,
            key.x + key.width, key.y + key.height
        )
        onBufferDraw()
        invalidate(
            key.x, key.y,
            key.x + key.width, key.y + key.height
        )
    }

    private fun openPopupIfRequired(me: MotionEvent): Boolean {
        // Check if we have a popup layout specified first.
        if (mPopupLayout == 0) {
            return false
        }

        if (mCurrentKey < 0 || mCurrentKey >= mKeys.size) {
            return false
        }

        val popupKey = mKeys[mCurrentKey]
        val result = onLongPress(popupKey, me)
        if (result) {
            mAbortKey = true
            showPreview(NOT_A_KEY)
        }

        return result
    }

    /**
     * Called when a key is long pressed. By default this will open any popup keyboard associated with this key through the attributes
     * popupLayout and popupCharacters.
     * @param popupKey the key that was long pressed
     * @return true if the long press is handled, false otherwise. Subclasses should call the method on the base class if the subclass doesn't wish to
     * handle the call.
     */
    private fun onLongPress(popupKey: MyKeyboard.Key, me: MotionEvent): Boolean {
        val popupKeyboardId = popupKey.popupResId
        if (popupKeyboardId != 0) {
            mMiniKeyboardContainer = mMiniKeyboardCache[popupKey]
            if (mMiniKeyboardContainer == null) {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                mMiniKeyboardContainer = inflater.inflate(mPopupLayout, null)
                mMiniKeyboard =
                    mMiniKeyboardContainer!!.findViewById<View>(R.id.mini_keyboard_view) as MyKeyboardView

                mMiniKeyboard!!.mOnKeyboardActionListener = object : OnKeyboardActionListener {
                    override fun onKey(code: Int) {
                        (mOnKeyboardActionListener ?: return).onKey(code)
                        dismissPopupKeyboard()
                    }

                    override fun onPress(primaryCode: Int) {
                        (mOnKeyboardActionListener ?: return).onPress(primaryCode)
                    }

                    override fun onActionUp() {
                        (mOnKeyboardActionListener ?: return).onActionUp()
                    }

                    override fun moveCursorLeft() {
                        (mOnKeyboardActionListener ?: return).moveCursorLeft()
                    }

                    override fun moveCursorRight() {
                        (mOnKeyboardActionListener ?: return).moveCursorRight()
                    }

                    override fun onText(text: String) {
                        (mOnKeyboardActionListener ?: return).onText(text)
                    }
                }

                val keyboard = if (popupKey.popupCharacters != null) {
                    MyKeyboard(context, popupKeyboardId, popupKey.popupCharacters!! as String, popupKey.width)
                } else {
                    MyKeyboard(context, popupKeyboardId, 0)
                }

                mMiniKeyboard!!.setKeyboard(keyboard)
                mPopupParent = this
                mMiniKeyboardContainer!!.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
                )
                mMiniKeyboardCache[popupKey] = mMiniKeyboardContainer
            } else {
                mMiniKeyboard =
                    mMiniKeyboardContainer!!.findViewById<View>(R.id.mini_keyboard_view) as MyKeyboardView
            }

            getLocationInWindow(mCoordinates)
            mPopupX = popupKey.x
            mPopupY = popupKey.y

            val widthToUse =
                mMiniKeyboardContainer!!.measuredWidth - (popupKey.popupCharacters!!.length / 2) * popupKey.width
            mPopupY -= mMiniKeyboardContainer!!.measuredHeight
            val x = mPopupX
            val y = mPopupY + mCoordinates[1]
            val xOffset = max(0, x)
            mMiniKeyboard!!.setPopupOffset(-xOffset, y)

            // make sure we highlight the proper key right after long pressing it, before any ACTION_MOVE event occurs
            val miniKeyboardX = if (xOffset + mMiniKeyboard!!.measuredWidth <= measuredWidth) {
                xOffset
            } else {
                measuredWidth - mMiniKeyboard!!.measuredWidth
            }

            val keysCnt = mMiniKeyboard!!.mKeys.size
            var selectedKeyIndex =
                floor((me.x - miniKeyboardX) / popupKey.width.toDouble()).toInt()
            if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                selectedKeyIndex += MAX_KEYS_PER_MINI_ROW
            }
            selectedKeyIndex = max(0, min(selectedKeyIndex, keysCnt - 1))

            for (i in 0 until keysCnt) {
                mMiniKeyboard!!.mKeys[i].focused = i == selectedKeyIndex
            }

            mMiniKeyboardSelectedKeyIndex = selectedKeyIndex
            mMiniKeyboard!!.invalidateAllKeys()

            val miniShiftStatus = if (isShifted()) SHIFT_ON_PERMANENT else SHIFT_OFF
            mMiniKeyboard!!.setShifted(miniShiftStatus)
            mPopupKeyboard.contentView = mMiniKeyboardContainer
            mPopupKeyboard.width = mMiniKeyboardContainer!!.measuredWidth
            mPopupKeyboard.height = mMiniKeyboardContainer!!.measuredHeight
            mPopupKeyboard.showAtLocation(this, Gravity.NO_GRAVITY, x, y)
            mMiniKeyboardOnScreen = true
            invalidateAllKeys()
            return true
        }
        return false
    }

    override fun onTouchEvent(me: MotionEvent): Boolean {
        val action = me.action

        if (ignoreTouches) {
            if (action == MotionEvent.ACTION_UP) {
                ignoreTouches = false

                // fix a glitch with long pressing backspace, then clicking some letter
                if (mRepeatKeyIndex != NOT_A_KEY) {
                    val key = mKeys[mRepeatKeyIndex]
                    if (key.code == KEYCODE_DELETE) {
                        mHandler?.removeMessages(MSG_REPEAT)
                        mRepeatKeyIndex = NOT_A_KEY
                    }
                }
            }
            return true
        }

        // handle moving between alternative popup characters by swiping
        if (mPopupKeyboard.isShowing) {
            when (action) {
                MotionEvent.ACTION_MOVE -> {
                    if (mMiniKeyboard != null) {

                        // Just take one single key.
                        mMiniKeyboardSelectedKeyIndex = 0
                        mMiniKeyboard!!.invalidateAllKeys()

/*                        val coords = intArrayOf(0, 0)
                        mMiniKeyboard!!.getLocationOnScreen(coords)
                        val keysCnt = mMiniKeyboard!!.mKeys.size
                        val lastRowKeyCount = if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                            max(keysCnt % MAX_KEYS_PER_MINI_ROW, 1)
                        } else {
                            keysCnt
                        }

                        val widthPerKey = if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                            mMiniKeyboard!!.width / MAX_KEYS_PER_MINI_ROW
                        } else {
                            mMiniKeyboard!!.width / lastRowKeyCount
                        }

                        var selectedKeyIndex =
                            floor((me.x - coords[0]) / widthPerKey.toDouble()).toInt()
                        if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                            selectedKeyIndex = max(0, selectedKeyIndex)
                            selectedKeyIndex += MAX_KEYS_PER_MINI_ROW
                        }

                        selectedKeyIndex = max(0, min(selectedKeyIndex, keysCnt - 1))
                        if (selectedKeyIndex != mMiniKeyboardSelectedKeyIndex) {
                            for (i in 0 until keysCnt) {
                                mMiniKeyboard!!.mKeys[i].focused = i == selectedKeyIndex
                            }
                            mMiniKeyboardSelectedKeyIndex = selectedKeyIndex
                            mMiniKeyboard!!.invalidateAllKeys()
                        }*/

                      /*  if (coords[0] > 0 || coords[1] > 0) {
                            if (coords[0] - me.x > mPopupMaxMoveDistance ||                                         // left
                                me.x - (coords[0] + mMiniKeyboard!!.measuredWidth) > mPopupMaxMoveDistance          // right
                            ) {
                                dismissPopupKeyboard()
                            }
                        }*/
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mMiniKeyboard?.mKeys?.firstOrNull { it.focused }?.apply {
                        (mOnKeyboardActionListener ?: return@apply).onKey(code)
                    }
                    mMiniKeyboardSelectedKeyIndex = -1
                    dismissPopupKeyboard()
                }
            }
        }

        return onModifiedTouchEvent(me)
    }

    private fun onModifiedTouchEvent(me: MotionEvent): Boolean {
        var touchX = me.x.toInt()
        var touchY = me.y.toInt()
        if (touchY >= -mVerticalCorrection) {
            touchY += mVerticalCorrection
        }

        val action = me.actionMasked
        val eventTime = me.eventTime
        val keyIndex = getPressedKeyIndex(touchX, touchY)

        // Ignore all motion events until a DOWN.
        if (mAbortKey && action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL) {
            return true
        }

        // Needs to be called after the gesture detector gets a turn, as it may have displayed the mini keyboard
        if (mMiniKeyboardOnScreen && action != MotionEvent.ACTION_CANCEL) {
            return true
        }

        when (action) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                // if the user presses a key while still holding down the previous, type in both chars and ignore the later gestures
                // can happen at fast typing, easier to reproduce by increasing LONGPRESS_TIMEOUT
                ignoreTouches = true
                mHandler!!.removeMessages(MSG_LONGPRESS)
                dismissPopupKeyboard()
                detectAndSendKey(keyIndex, me.x.toInt(), me.y.toInt(), eventTime)

                val newPointerX = me.getX(1).toInt()
                val newPointerY = me.getY(1).toInt()
                val secondKeyIndex = getPressedKeyIndex(newPointerX, newPointerY)
                showPreview(secondKeyIndex)
                detectAndSendKey(secondKeyIndex, newPointerX, newPointerY, eventTime)

                val secondKeyCode = mKeys.getOrNull(secondKeyIndex)?.code
                if (secondKeyCode != null) {
                    mOnKeyboardActionListener!!.onPress(secondKeyCode)
                }

                showPreview(NOT_A_KEY)
                invalidateKey(mCurrentKey)
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                mAbortKey = false
                mLastCodeX = touchX
                mLastCodeY = touchY
                mLastKeyTime = 0
                mCurrentKeyTime = 0
                mLastKey = NOT_A_KEY
                mCurrentKey = keyIndex
                mDownTime = me.eventTime
                mLastMoveTime = mDownTime

                val onPressKey = if (keyIndex != NOT_A_KEY) {
                    mKeys[keyIndex].code
                } else {
                    0
                }

                mOnKeyboardActionListener!!.onPress(onPressKey)

                var wasHandled = false
                if (mCurrentKey >= 0 && mKeys[mCurrentKey].repeatable) {
                    mRepeatKeyIndex = mCurrentKey

                    val msg = mHandler!!.obtainMessage(MSG_REPEAT)
                    mHandler!!.sendMessageDelayed(msg, REPEAT_START_DELAY.toLong())
                    // if the user long presses Space, move the cursor after swipine left/right
                    if (mKeys[mCurrentKey].code == KEYCODE_SPACE) {
                        mLastSpaceMoveX = -1
                    } else {
                        repeatKey(true)
                    }

                    // Delivering the key could have caused an abort
                    if (mAbortKey) {
                        mRepeatKeyIndex = NOT_A_KEY
                        wasHandled = true
                    }
                }

                if (!wasHandled && mCurrentKey != NOT_A_KEY) {
                    val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                    mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                }

                if (mPopupParent.id != R.id.mini_keyboard_view) {
                    showPreview(keyIndex)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                var continueLongPress = false
                if (keyIndex != NOT_A_KEY) {
                    if (mCurrentKey == NOT_A_KEY) {
                        mCurrentKey = keyIndex
                        mCurrentKeyTime = eventTime - mDownTime
                    } else {
                        if (keyIndex == mCurrentKey) {
                            mCurrentKeyTime += eventTime - mLastMoveTime
                            continueLongPress = true
                        } else if (mRepeatKeyIndex == NOT_A_KEY) {
                            mLastKey = mCurrentKey
                            mLastCodeX = mLastX
                            mLastCodeY = mLastY
                            mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                            mCurrentKey = keyIndex
                            mCurrentKeyTime = 0
                        }
                    }
                }

                if (mIsLongPressingSpace) {
                    if (mLastSpaceMoveX == -1) {
                        mLastSpaceMoveX = mLastX
                    }

                    val diff = mLastX - mLastSpaceMoveX
                    if (diff < -mSpaceMoveThreshold) {
                        for (i in diff / mSpaceMoveThreshold until 0) {
                            mOnKeyboardActionListener?.moveCursorLeft()
                        }
                        mLastSpaceMoveX = mLastX
                    } else if (diff > mSpaceMoveThreshold) {
                        for (i in 0 until diff / mSpaceMoveThreshold) {
                            mOnKeyboardActionListener?.moveCursorRight()
                        }
                        mLastSpaceMoveX = mLastX
                    }
                } else if (!continueLongPress) {
                    // Cancel old longpress
                    mHandler!!.removeMessages(MSG_LONGPRESS)
                    // Start new longpress if key has changed
                    if (keyIndex != NOT_A_KEY) {
                        val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                        mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                    }

                    if (mPopupParent.id != R.id.mini_keyboard_view) {
                        showPreview(mCurrentKey)
                    }
                    mLastMoveTime = eventTime
                }
            }
            MotionEvent.ACTION_UP -> {
                mLastSpaceMoveX = 0
                removeMessages()
                if (keyIndex == mCurrentKey) {
                    mCurrentKeyTime += eventTime - mLastMoveTime
                } else {
                    mLastKey = mCurrentKey
                    mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                    mCurrentKey = keyIndex
                    mCurrentKeyTime = 0
                }

                if (mCurrentKeyTime < mLastKeyTime && mCurrentKeyTime < DEBOUNCE_TIME && mLastKey != NOT_A_KEY) {
                    mCurrentKey = mLastKey
                    touchX = mLastCodeX
                    touchY = mLastCodeY
                }
                showPreview(NOT_A_KEY)
                Arrays.fill(mKeyIndices, NOT_A_KEY)
                // If we're not on a repeating key (which sends on a DOWN event)
                if (mRepeatKeyIndex == NOT_A_KEY && !mMiniKeyboardOnScreen && !mAbortKey) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                }

                if (mKeys.getOrNull(mCurrentKey)?.code == KEYCODE_SPACE && !mIsLongPressingSpace) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                }

                invalidateKey(keyIndex)
                mRepeatKeyIndex = NOT_A_KEY
                mOnKeyboardActionListener!!.onActionUp()
                mIsLongPressingSpace = false
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsLongPressingSpace = false
                mLastSpaceMoveX = 0
                removeMessages()
                dismissPopupKeyboard()
                mAbortKey = true
                showPreview(NOT_A_KEY)
                invalidateKey(mCurrentKey)
            }
        }

        mLastX = touchX
        mLastY = touchY
        return true
    }

    private fun repeatKey(initialCall: Boolean): Boolean {
        val key = mKeys[mRepeatKeyIndex]
        if (!initialCall && key.code == KEYCODE_SPACE) {
            if (!mIsLongPressingSpace) {
                vibrateIfNeeded()
            }

            mIsLongPressingSpace = true
        } else {
            detectAndSendKey(mCurrentKey, key.x, key.y, mLastTapTime)
        }
        return true
    }

    fun closeClipboardManager() {
        mClipboardManagerHolder?.clipboard_manager_holder?.beGone()
    }

    fun closeWindowManager() {
        mWindowManager?.removeView(keyboard_holder)
    }

    fun openClipboardManager() {
        (mClipboardManagerHolder ?: return).clipboard_manager_holder.beVisible()
        setupStoredClips()
    }

    fun setupStoredClips() {
        ensureBackgroundThread {
            val clips = ArrayList<ListItem>()
            val clipboardContent = context.getCurrentClip()

            val pinnedClips = context.clipsDB.getClips().reversed()

            if (clipboardContent?.isNotEmpty() == true) {
                val section = ClipsSectionLabel(context.getString(R.string.clipboard_current), true)
                clips.add(section)

                val clip = Clip(-1, clipboardContent)
                //clips.add(clip)

                // Add new clips to history list.
                if (!clipboard_history_list.contains(clip)) {
                    clipboard_history_list.add(clip)
                }

                // Display list.
                clipboard_history_list.reversed().forEach() {
                    clips.add(it)
                }

                // Limit the clipboard history list with N entries.
                if (clipboard_history_list.size >= CLIPBOARD_HISTORY_LIMIT) {
                    clipboard_history_list.removeFirst()
                }

            }

            if (pinnedClips.isNotEmpty() == true) {
                val section = ClipsSectionLabel(context.getString(R.string.clipboard_pinned), false)
                clips.add(section)
            }

            clips.addAll(pinnedClips)
            Handler(Looper.getMainLooper()).post {
                setupClipsAdapter(clips)
            }
        }
    }

    private fun setupClipsAdapter(clips: ArrayList<ListItem>) {
        mClipboardManagerHolder?.apply {
            clipboard_content_placeholder_1.beVisibleIf(clips.isEmpty())
            clipboard_content_placeholder_2.beVisibleIf(clips.isEmpty())
            clips_list.beVisibleIf(clips.isNotEmpty())
        }

        val refreshClipsListener = object : RefreshClipsListener {
            override fun refreshClips() {
                setupStoredClips()
            }
        }

        val adapter = ClipsKeyboardAdapter(context, clips, refreshClipsListener) { clip ->
            (mOnKeyboardActionListener ?: return@ClipsKeyboardAdapter).onText(clip.value)
            vibrateIfNeeded()
        }

        mClipboardManagerHolder?.clips_list?.adapter = adapter
    }


    fun openEmojiPalette() {
        (mEmojiPaletteHolder ?: return).emoji_palette_holder.beVisible()
        setupEmojis()
    }

    private fun closeEmojiPalette() {
        mEmojiPaletteHolder?.apply {
            emoji_palette_holder?.beGone()
            mEmojiPaletteHolder?.emojis_list?.scrollToPosition(0)
        }
    }

    private fun setupEmojis() {
        ensureBackgroundThread {
            val fullEmojiList = parseRawEmojiSpecsFile(context, EMOJI_SPEC_FILE_PATH)
            val systemFontPaint = Paint().apply {
                typeface = Typeface.DEFAULT
            }

            val emojis = fullEmojiList.filter { emoji ->
                systemFontPaint.hasGlyph(emoji) || (EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED && EmojiCompat.get()
                    .getEmojiMatch(emoji, emojiCompatMetadataVersion) == EMOJI_SUPPORTED)
            }

            Handler(Looper.getMainLooper()).post {
                setupEmojiAdapter(emojis)
            }
        }
    }

    private fun setupEmojiAdapter(emojis: List<String>) {
        mEmojiPaletteHolder?.emojis_list?.apply {
            val emojiItemWidth = context.resources.getDimensionPixelSize(R.dimen.emoji_item_size)

            layoutManager = AutoGridLayoutManager(context, emojiItemWidth)
            adapter = EmojisAdapter(context = context, items = emojis) { emoji ->
                (mOnKeyboardActionListener ?: return@EmojisAdapter).onText(emoji)
                vibrateIfNeeded()
            }
        }
    }

    private fun closing() {
        if (mPreviewPopup.isShowing) {
            mPreviewPopup.dismiss()
        }
        removeMessages()
        dismissPopupKeyboard()
        mBuffer = null
        mCanvas = null
        mMiniKeyboardCache.clear()
    }

    private fun removeMessages() {
        mHandler?.apply {
            removeMessages(MSG_REPEAT)
            removeMessages(MSG_LONGPRESS)
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        closing()
    }

    private fun dismissPopupKeyboard() {
        if (mPopupKeyboard.isShowing) {
            mPopupKeyboard.dismiss()
            mMiniKeyboardOnScreen = false
            invalidateAllKeys()
        }
    }
}
