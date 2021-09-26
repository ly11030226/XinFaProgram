package com.szty.h5xinfa.baoao

import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.szty.h5xinfa.R
import com.szty.h5xinfa.ui.CodeActivity
import com.szty.h5xinfa.ui.MainActivity
import java.io.File
import java.lang.Exception
import java.nio.charset.Charset


const val TYPE_JPG = "bg.jpg"
const val TYPE_PNG = "bg.png"

var bgBitmapDrawable: BitmapDrawable? = null
const val KEY_TEXT_SIZE = "KEY_TEXT_SIZE"
const val DEFAULT_TEXT_SIZE = 50f
const val KEY_TEXT_COLOR = "KEY_TEXT_COLOR"
const val DEFAULT_TEXT_COLOR = "255,255,255"
const val KEY_FIRST_WINDOW = "KEY_FIRST_WINDOW"
const val DEFAULT_FIRST_WINDOW = "174,5,44"
const val KEY_SECOND_WINDOW = "KEY_SECOND_WINDOW"
const val DEFAULT_SECOND_WINDOW = "167,130,75"
const val KEY_THIRD_WINDOW = "KEY_THIRD_WINDOW"
const val DEFAULT_THIRD_WINDOW = "209,73,205"
const val KEY_FORTH_WINDOW = "KEY_FORTH_WINDOW"
const val DEFAULT_FORTH_WINDOW = "183,80,81"
const val KEY_SERVER_IP = "KEY_SERVER_IP"
const val DEFAULT_SERVER_IP = "192.168.0.186"
const val KEY_SERVER_PORT = "KEY_SERVER_PORT"
const val DEFAULT_SERVER_PORT = "1974"
const val KEY_WINDOW_NUM = "KEY_WINDOW_NUM"
const val DEFAULT_WINDOW_NUM = 1

const val KEY_RESULT = "KEY_RESULT"
const val ACTION_RESULT = "ACTION_RESULT"
const val HEX_55 = 0X55
const val HEX_21 = 0X21
const val HEX_02 = 0X02

const val SHOW_QUEUE_TIME = 5 * 60 * 1000
const val SHOW_QUEUE = 0x33
const val REQUEST_CODE = 0x55
const val FILE_CONFIG_JSON = "ConfigJson.txt"
const val RESULT_CODE_EXIT = 0x88
const val FILE_CODE = "code.txt"
var identifyStr: String? = null
const val SZTY = "_"

fun transformColor(rgbStr: String): Int {
    return if (rgbStr.contains(",")) {
        val str: List<String> = rgbStr.split(",")
        val r = Integer.toHexString(str[0].toInt())
        val g = Integer.toHexString(str[1].toInt())
        val b = Integer.toHexString(str[2].toInt())
        val cod = "#${addCode(r)}${addCode(g)}${addCode(b)}"
        Log.i("color", "transformColor: $cod")
        return Color.parseColor(cod)
    } else {
        Color.rgb(255, 0, 0)
    }
}

fun addCode(str: String): String {
    return if (str.length == 1) {
        "0$str"
    } else {
        str
    }
}


/**
 * 获取手机的设备号.
 * @param context 上下文
 * @return 设备号
 */
@SuppressLint("HardwareIds")
fun getIMEIDeviceId(context: Context): String? {
    val deviceId: String
    //如果sdk版本大于等于29
    deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    } else {
        val mTelephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(READ_PHONE_STATE) !== PackageManager.PERMISSION_GRANTED) {
                return ""
            }
        }
        assert(mTelephony != null) {
            throw Exception("TelephonyManager is null")
            L.e("TelephonyManager is null")
        }
        if (mTelephony.deviceId != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mTelephony.imei
            } else {
                mTelephony.deviceId
            }
        } else {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
    return deviceId
}


