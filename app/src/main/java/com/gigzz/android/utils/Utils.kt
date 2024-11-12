package com.gigzz.android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import java.io.*
import java.net.URLEncoder
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

/**
 *
 * Created by Somnath_Verma
 * on
 * 22-02-2021
 *
 **/

private const val TAG = "Utils"


fun getUploadProfileImageName(): String {
    return getImageUploadPath(AppConstants.PROFILE_IMG_PATH) + "img_" + System.currentTimeMillis() + ".jpeg"
}

fun getTimezone(): String {
    val timeZone = TimeZone.getDefault()
    return timeZone.id
}

fun getTimeOffset(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault())
    val currentLocalTime: Date = calendar.time
    //val date: DateFormat = SimpleDateFormat("z", Locale.getDefault())
    val date: DateFormat = SimpleDateFormat("ZZZZZ", Locale.getDefault())

    return date.format(currentLocalTime).removePrefix("GMT")
}



fun getDateTimeString(datetime: String): String {
    var date: Date? = null
    var outputString = ""
    try {
        date = SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.ENGLISH).parse(datetime)
        //date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH).parse(datetime)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    date?.let {
        val day = SimpleDateFormat("dd", Locale.ENGLISH).format(it)
        val time = SimpleDateFormat("MMM yyyy, hh:mm a", Locale.ENGLISH).format(it)

        outputString = "${getDayOfMonthSuffix(day.toInt())} $time"
    }
    return outputString
}

fun getTimeAmPm(datetime: String): String {
    var date: Date? = null
    var outputString = ""
    try {
        date = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(datetime)
        //date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH).parse(datetime)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    date?.let {
        val day = SimpleDateFormat("dd", Locale.ENGLISH).format(it)
        val time = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(it)

        //outputString = "${getDayOfMonthSuffix(day.toInt())} $time"
        outputString = time
    }
    return outputString
}

fun changeDateFormat(datetime: String): String {
    var date: Date? = null
    var outputString = ""
    try {
        date = SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(datetime)
        //date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH).parse(datetime)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    date?.let {
        val day = SimpleDateFormat("dd", Locale.ENGLISH).format(it)
        val time = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(it)

        //outputString = "${getDayOfMonthSuffix(day.toInt())} $time"
        outputString = time
    }
    return outputString
}


fun getDayOfMonthSuffix(n: Int): String {
    val daySuffix = if (n in 11..13) {
        "th"
    } else when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$n$daySuffix"
}

fun timestampToMSS(position: Long): String {
    val totalSeconds = floor(position / 1E3).toInt()
    return (getTwoDecimalsValue(totalSeconds / 60) + ":"
            + getTwoDecimalsValue(totalSeconds % 60))
}

fun getTwoDecimalsValue(value: Int): String {
    return if (value in 0..9) {
        "0$value"
    } else {
        value.toString()
    }
}


@SuppressLint("SimpleDateFormat")
@Throws(ParseException::class)
fun formatToYesterdayOrToday(date: String?): String? {
    val dateTime = SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(date)
    val calendar = Calendar.getInstance()
    calendar.time = dateTime
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    val tomorrow = Calendar.getInstance()
    yesterday.add(Calendar.DATE, -1)
    tomorrow.add(Calendar.DATE, 1)
    val timeFormatter: DateFormat = SimpleDateFormat("hh:mm a")
    val timeFormatter2: DateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a")
    return if (calendar[Calendar.YEAR] === today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] === today[Calendar.DAY_OF_YEAR]) {
        timeFormatter.format(dateTime).uppercase(Locale.getDefault()) +", Today"
    } else if (calendar[Calendar.YEAR] === yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] === yesterday[Calendar.DAY_OF_YEAR]) {
        timeFormatter.format(dateTime).uppercase(Locale.getDefault()) +", Yesterday"
    } else if (calendar[Calendar.YEAR] === tomorrow[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] === tomorrow[Calendar.DAY_OF_YEAR]) {
        timeFormatter.format(dateTime).uppercase(Locale.getDefault()) +", Tomorrow"
    }
    else {
        if (timeFormatter2.format(dateTime).contains("am")) timeFormatter2.format(dateTime).replace("am","AM")
        else timeFormatter2.format(dateTime).replace("pm","PM")
    }
}

@SuppressLint("SimpleDateFormat")
@Throws(ParseException::class)
fun formatToTomorrowOrToday(date: String?): String? {
    val dateTime = SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(date)
    val calendar = Calendar.getInstance()
    calendar.time = dateTime
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    val tomorrow = Calendar.getInstance()
    yesterday.add(Calendar.DATE, -1)
    tomorrow.add(Calendar.DATE, 1)
    val timeFormatter: DateFormat = SimpleDateFormat("hh:mm a")
    val timeFormatter2: DateFormat = SimpleDateFormat("dd MMM, hh:mm a")
    return if (calendar[Calendar.YEAR] === today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] === today[Calendar.DAY_OF_YEAR]) {
        "Today, "+timeFormatter.format(dateTime).uppercase(Locale.getDefault())
    } else if (calendar[Calendar.YEAR] === yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] === yesterday[Calendar.DAY_OF_YEAR]) {
        "Yesterday, "+timeFormatter.format(dateTime).uppercase(Locale.getDefault())
    } else if (calendar[Calendar.YEAR] === tomorrow[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] === tomorrow[Calendar.DAY_OF_YEAR]) {
        "Tomorrow, "+timeFormatter.format(dateTime).uppercase(Locale.getDefault())
    }
    else {
        if (timeFormatter2.format(dateTime).contains("am")) timeFormatter2.format(dateTime).replace("am","AM")
        else timeFormatter2.format(dateTime).replace("pm","PM")
    }
}

fun getCurrentDate():String{
    val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
    // val sdf2 = SimpleDateFormat("MM/dd/yyyy hh:mm a")
    var currentDate=""
    try {
        currentDate = sdf.format(Date())

    }catch (E:Exception){}
    return currentDate
}

fun String.toDate(
    dateFormat: String = "yyyy-MM-dd HH:mm:ss",
    timeZone: TimeZone = TimeZone.getTimeZone("UTC")
): Date {
    val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this)
}
fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}

fun daysLeftDateTime(dateTime: String): String? {
    var setDate = ""
    try {
        val format = SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
        val past = format.parse(dateTime)
        val now = Date()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)
        val days = TimeUnit.MILLISECONDS.toDays(now.time - past.time)
        setDate = "$days days left"

    } catch (j: java.lang.Exception) {
        j.printStackTrace()
    }
    return setDate
}

fun loadBitmapFromView(v: View, width: Int, height: Int): Bitmap {
    val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    v.draw(c)
    return b
}

fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    var image = image
    return if (maxHeight > 0 && maxWidth > 0) {
        val width = image.width
        val height = image.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        image
    } else {
        image
    }
}

fun getYearOnly(date: String): String {
    return date.substring(6, date.length)
}

fun getCombinedDateWithStartAndEndDate(startDate: String, endDate: String): String {
    val finalStartDate = startDate.substring(0, 2)
    val finalStartMonth = startDate.substring(8, endDate.length)
    val finalEndDate = endDate.substring(0, 2)
    val finalEndMonth = endDate.substring(8, endDate.length)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    val startSuf = getDayOfMonthSuffix(finalStartDate.toInt())
    val endSuf = getDayOfMonthSuffix(finalEndDate.toInt())
    return "$startSuf $finalStartMonth - $endSuf $finalEndMonth"
}

fun String.isPhoneNumberValid(editText: EditText?): Boolean {
    val phonePattern = Regex("^[2-9]{1}[0-9]{4,9}$")
    val isValid = phonePattern.matches(this)
    if (!isValid) {
        editText?.error = "Invalid phone number"
    } else {
        editText?.error = null
    }
    return isValid
}

fun String.isZipCodeValid(): Boolean {
    val zipCodePattern = """^\d{5}(?:\d{1})?$""".toRegex()
    return zipCodePattern.matches(this) && !this.all { it == '0' }
}

fun formatDateTime(datetime: String, inputFormat: String, outputFormat: String): String {
    val dateFormat = SimpleDateFormat(inputFormat, Locale.ENGLISH).parse(datetime)
    val format = SimpleDateFormat(outputFormat, Locale.ENGLISH)
    return format.format(dateFormat!!)
}