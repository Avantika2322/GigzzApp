package com.gigzz.android.utils


import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Context.appVersion(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        //
    }.toString()
}

fun Activity.setFullScreen() {
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
    window.statusBarColor = Color.TRANSPARENT
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true)
}

fun Activity.fullScreen() {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        statusBarColor = Color.TRANSPARENT
    }
}

fun Activity.setStatusBarColor(context: Context, color: Int) {
    setSystemBarTheme(false)
    window.statusBarColor = ContextCompat.getColor(context, color)
}

fun getStatusBarHeight(context: Context): Int {
    var result = 0
    val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun View.setMarginTop(marginTop: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, marginTop, 0, 0)
    this.layoutParams = menuLayoutParams
}

fun Activity.setWindowFlag(bits: Int, on: Boolean) {
    val win = window
    val winParams = win.attributes
    if (on) {
        winParams.flags = winParams.flags or bits
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    win.attributes = winParams
}

fun Activity.setSystemBarTheme(pIsDark: Boolean) {
    val lFlags = window.decorView.systemUiVisibility
    window.decorView.systemUiVisibility = if (pIsDark) {
        lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    } else {
        lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun hideKeyboard(view: View?) {
    val inputMethodManager =
        view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showKeyBoard(context: Context, view: View) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED)
}

fun Activity.disableUserInteraction() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}

fun Activity.enableUserInteraction() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            Log.i("isMyServiceRunning?", true.toString() + "")
            return true
        }
    }
    Log.i("isMyServiceRunning?", false.toString() + "")
    return false
}

fun openDialer(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phoneNumber")
    context.startActivity(Intent.createChooser(intent, "Dial Number"))
}

fun startCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = Uri.parse("tel:$phoneNumber")
    context.startActivity(Intent.createChooser(intent, "Call Number"))
}

fun postCreatedDateTime(dateTime: String): String? {
    var setDate = ""
    try {
        val format = SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault())
        val past = format.parse(dateTime)
        val now = Date()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
        val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)

        setDate = when {
            seconds < 60 -> "Just Now"
            minutes < 60 -> "$minutes m ago"
            hours < 24 -> "$hours h ago"
            days < 7 -> "$days d ago"
            days < 14 -> "1 w ago"
            days < 21 -> "2 w ago"
            days < 30 -> "3 w ago"
            days < 60 -> "1 M ago"
            days < 90 -> "2 M ago"
            days < 120 -> "3 M ago"
            days < 150 -> "4 M ago"
            days < 180 -> "5 M ago"
            days < 210 -> "6 M ago"
            days < 240 -> "7 M ago"
            days < 270 -> "8 M ago"
            days < 300 -> "9 M ago"
            days < 330 -> "10 M ago"
            days < 365 -> "11 M ago"
            days < 730 -> "1 y ago"
            days < 1095 -> "2 y ago"
            else -> dateTime.substring(0, 10)
        }

    } catch (j: java.lang.Exception) {
        j.printStackTrace()
    }
    return setDate
}