package com.simplemobiletools.commons.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import androidx.annotation.ChecksSdkIntAtLeast
import com.simplemobiletools.keyboard.R

const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY: String = "com.android.externalstorage.documents"
const val EXTRA_SHOW_ADVANCED: String = "android.content.extra.SHOW_ADVANCED"

const val APP_NAME: String = "app_name"
const val APP_LICENSES: String = "app_licenses"
const val APP_VERSION_NAME: String = "app_version_name"
const val APP_ICON_IDS: String = "app_icon_ids"
const val APP_ID: String = "app_id"
const val APP_LAUNCHER_NAME: String = "app_launcher_name"
const val INVALID_NAVIGATION_BAR_COLOR: Int = -1
const val SAVE_DISCARD_PROMPT_INTERVAL: Long = 1000L
const val SD_OTG_PATTERN: String = "^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val SD_OTG_SHORT: String = "^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val DARK_GREY: Int = 0xFF333333.toInt()

const val MEDIUM_ALPHA: Float = 0.5f
const val HIGHER_ALPHA: Float = 0.75f

// shared preferences
const val PREFS_KEY: String = "Prefs"
const val APP_RUN_COUNT: String = "app_run_count"
const val SD_TREE_URI: String = "tree_uri_2"
const val PRIMARY_ANDROID_DATA_TREE_URI: String = "primary_android_data_tree_uri_2"
const val OTG_ANDROID_DATA_TREE_URI: String = "otg_android_data_tree__uri_2"
const val SD_ANDROID_DATA_TREE_URI: String = "sd_android_data_tree_uri_2"
const val PRIMARY_ANDROID_OBB_TREE_URI: String = "primary_android_obb_tree_uri_2"
const val OTG_ANDROID_OBB_TREE_URI: String = "otg_android_obb_tree_uri_2"
const val SD_ANDROID_OBB_TREE_URI: String = "sd_android_obb_tree_uri_2"
const val OTG_TREE_URI: String = "otg_tree_uri_2"
const val SD_CARD_PATH: String = "sd_card_path_2"
const val OTG_REAL_PATH: String = "otg_real_path_2"
const val INTERNAL_STORAGE_PATH: String = "internal_storage_path"
const val TEXT_COLOR: String = "text_color"
const val BACKGROUND_COLOR: String = "background_color"
const val PRIMARY_COLOR: String = "primary_color_2"
const val ACCENT_COLOR: String = "accent_color"
const val APP_ICON_COLOR: String = "app_icon_color"
const val NAVIGATION_BAR_COLOR: String = "navigation_bar_color"
const val DEFAULT_NAVIGATION_BAR_COLOR: String = "default_navigation_bar_color"
const val LAST_ICON_COLOR: String = "last_icon_color"
const val CUSTOM_TEXT_COLOR: String = "custom_text_color"
const val CUSTOM_BACKGROUND_COLOR: String = "custom_background_color"
const val CUSTOM_PRIMARY_COLOR: String = "custom_primary_color"
const val CUSTOM_ACCENT_COLOR: String = "custom_accent_color"
const val CUSTOM_NAVIGATION_BAR_COLOR: String = "custom_navigation_bar_color"
const val CUSTOM_APP_ICON_COLOR: String = "custom_app_icon_color"
const val USE_ENGLISH: String = "use_english"
const val WAS_USE_ENGLISH_TOGGLED: String = "was_use_english_toggled"
const val IS_USING_SHARED_THEME: String = "is_using_shared_theme"
const val IS_USING_SYSTEM_THEME: String = "is_using_system_theme"
const val USE_24_HOUR_FORMAT: String = "use_24_hour_format"
const val OTG_PARTITION: String = "otg_partition_2"
const val IS_USING_MODIFIED_APP_ICON: String = "is_using_modified_app_icon"
const val WAS_ORANGE_ICON_CHECKED: String = "was_orange_icon_checked"
const val APP_SIDELOADING_STATUS: String = "app_sideloading_status"
const val DATE_FORMAT: String = "date_format"
const val FONT_SIZE: String = "font_size"
const val FAVORITES: String = "favorites"
internal const val COLOR_PICKER_RECENT_COLORS = "color_picker_recent_colors"

// licenses
internal const val LICENSE_KOTLIN = 1L
const val LICENSE_SUBSAMPLING: Long = 2L
const val LICENSE_GLIDE: Long = 4L
const val LICENSE_CROPPER: Long = 8L
const val LICENSE_FILTERS: Long = 16L
const val LICENSE_RTL: Long = 32L
const val LICENSE_JODA: Long = 64L
const val LICENSE_STETHO: Long = 128L
const val LICENSE_OTTO: Long = 256L
const val LICENSE_PHOTOVIEW: Long = 512L
const val LICENSE_PICASSO: Long = 1024L
const val LICENSE_PATTERN: Long = 2048L
const val LICENSE_REPRINT: Long = 4096L
const val LICENSE_GIF_DRAWABLE: Long = 8192L
const val LICENSE_AUTOFITTEXTVIEW: Long = 16384L
const val LICENSE_ROBOLECTRIC: Long = 32768L
const val LICENSE_ESPRESSO: Long = 65536L
const val LICENSE_GSON: Long = 131072L
const val LICENSE_LEAK_CANARY: Long = 262144L
const val LICENSE_NUMBER_PICKER: Long = 524288L
const val LICENSE_EXOPLAYER: Long = 1048576L
const val LICENSE_PANORAMA_VIEW: Long = 2097152L
const val LICENSE_SANSELAN: Long = 4194304L
const val LICENSE_GESTURE_VIEWS: Long = 8388608L
const val LICENSE_INDICATOR_FAST_SCROLL: Long = 16777216L
const val LICENSE_EVENT_BUS: Long = 33554432L
const val LICENSE_AUDIO_RECORD_VIEW: Long = 67108864L
const val LICENSE_SMS_MMS: Long = 134217728L
const val LICENSE_APNG: Long = 268435456L
const val LICENSE_PDF_VIEWER: Long = 536870912L
const val LICENSE_M3U_PARSER: Long = 1073741824L
const val LICENSE_ANDROID_LAME: Long = 2147483648L

// global intents
const val OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB: Int = 1000
const val OPEN_DOCUMENT_TREE_OTG: Int = 1001
const val OPEN_DOCUMENT_TREE_SD: Int = 1002
const val OPEN_DOCUMENT_TREE_FOR_SDK_30: Int = 1003
const val CREATE_DOCUMENT_SDK_30: Int = 1008

const val SORT_BY_NAME: Int = 1
const val SORT_BY_DATE_MODIFIED: Int = 2
const val SORT_BY_SIZE: Int = 4
const val SORT_BY_EXTENSION: Int = 16
const val SORT_DESCENDING: Int = 1024
const val SORT_USE_NUMERIC_VALUE: Int = 32768

// permissions
const val PERMISSION_READ_STORAGE: Int = 1
const val PERMISSION_WRITE_STORAGE: Int = 2
const val PERMISSION_CAMERA: Int = 3
const val PERMISSION_RECORD_AUDIO: Int = 4
const val PERMISSION_READ_CONTACTS: Int = 5
const val PERMISSION_WRITE_CONTACTS: Int = 6
const val PERMISSION_READ_CALENDAR: Int = 7
const val PERMISSION_WRITE_CALENDAR: Int = 8
const val PERMISSION_CALL_PHONE: Int = 9
const val PERMISSION_READ_CALL_LOG: Int = 10
const val PERMISSION_WRITE_CALL_LOG: Int = 11
const val PERMISSION_GET_ACCOUNTS: Int = 12
const val PERMISSION_READ_SMS: Int = 13
const val PERMISSION_SEND_SMS: Int = 14
const val PERMISSION_READ_PHONE_STATE: Int = 15
const val PERMISSION_MEDIA_LOCATION: Int = 16
const val PERMISSION_POST_NOTIFICATIONS: Int = 17
const val PERMISSION_READ_MEDIA_IMAGES: Int = 18
const val PERMISSION_READ_MEDIA_VIDEO: Int = 19
const val PERMISSION_READ_MEDIA_AUDIO: Int = 20

// font sizes
const val FONT_SIZE_SMALL: Int = 0
const val FONT_SIZE_MEDIUM: Int = 1
const val FONT_SIZE_LARGE: Int = 2

const val SIDELOADING_UNCHECKED: Int = 0
const val SIDELOADING_TRUE: Int = 1
const val SIDELOADING_FALSE: Int = 2

val photoExtensions: Array<String> get() = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif", ".apng", ".avif")
val videoExtensions: Array<String> get() = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
val audioExtensions: Array<String> get() = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")

const val DATE_FORMAT_ONE: String = "dd.MM.yyyy"
const val DATE_FORMAT_TWO: String = "dd/MM/yyyy"
const val DATE_FORMAT_THREE: String = "MM/dd/yyyy"
const val DATE_FORMAT_FOUR: String = "yyyy-MM-dd"
const val DATE_FORMAT_FIVE: String = "d MMMM yyyy"
const val DATE_FORMAT_SIX: String = "MMMM d yyyy"
const val DATE_FORMAT_SEVEN: String = "MM-dd-yyyy"
const val DATE_FORMAT_EIGHT: String = "dd-MM-yyyy"

const val TIME_FORMAT_12: String = "hh:mm a"
const val TIME_FORMAT_24: String = "HH:mm"

val keyColorStrings: ArrayList<String> = arrayListOf(
    ".Red",
    ".Pink",
    ".Purple",
    ".Deep_purple",
    ".Indigo",
    ".Blue",
    ".Light_blue",
    ".Cyan",
    ".Teal",
    ".Green",
    ".Light_green",
    ".Lime",
    ".Yellow",
    ".Amber",
    ".Orange",
    ".Deep_orange",
    ".Brown",
    ".Blue_grey",
    ".Grey_black"
)

fun isOnMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
fun isMarshmallowPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun isNougatPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
fun isPiePlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isQPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isRPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isTiramisuPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

val normalizeRegex: Regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun getFilePlaceholderDrawables(context: Context): HashMap<String, Drawable> {
    val fileDrawables = HashMap<String, Drawable>()
    hashMapOf<String, Int>().apply {
        put("aep", R.drawable.ic_file_aep)
        put("ai", R.drawable.ic_file_ai)
        put("avi", R.drawable.ic_file_avi)
        put("css", R.drawable.ic_file_css)
        put("csv", R.drawable.ic_file_csv)
        put("dbf", R.drawable.ic_file_dbf)
        put("doc", R.drawable.ic_file_doc)
        put("docx", R.drawable.ic_file_doc)
        put("dwg", R.drawable.ic_file_dwg)
        put("exe", R.drawable.ic_file_exe)
        put("fla", R.drawable.ic_file_fla)
        put("flv", R.drawable.ic_file_flv)
        put("htm", R.drawable.ic_file_html)
        put("html", R.drawable.ic_file_html)
        put("ics", R.drawable.ic_file_ics)
        put("indd", R.drawable.ic_file_indd)
        put("iso", R.drawable.ic_file_iso)
        put("jpg", R.drawable.ic_file_jpg)
        put("jpeg", R.drawable.ic_file_jpg)
        put("js", R.drawable.ic_file_js)
        put("json", R.drawable.ic_file_json)
        put("m4a", R.drawable.ic_file_m4a)
        put("mp3", R.drawable.ic_file_mp3)
        put("mp4", R.drawable.ic_file_mp4)
        put("ogg", R.drawable.ic_file_ogg)
        put("pdf", R.drawable.ic_file_pdf)
        put("plproj", R.drawable.ic_file_plproj)
        put("prproj", R.drawable.ic_file_prproj)
        put("psd", R.drawable.ic_file_psd)
        put("rtf", R.drawable.ic_file_rtf)
        put("sesx", R.drawable.ic_file_sesx)
        put("sql", R.drawable.ic_file_sql)
        put("svg", R.drawable.ic_file_svg)
        put("txt", R.drawable.ic_file_txt)
        put("vcf", R.drawable.ic_file_vcf)
        put("wav", R.drawable.ic_file_wav)
        put("wmv", R.drawable.ic_file_wmv)
        put("xls", R.drawable.ic_file_xls)
        put("xml", R.drawable.ic_file_xml)
        put("zip", R.drawable.ic_file_zip)
    }.forEach { (key, value) ->
        fileDrawables[key] = context.resources.getDrawable(value)
    }
    return fileDrawables
}
