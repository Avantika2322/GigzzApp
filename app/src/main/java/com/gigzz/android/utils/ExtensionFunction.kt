package com.gigzz.android.utils


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.gigzz.android.R
import com.gigzz.android.domain.UserDataSerializer
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern
import kotlin.math.ln
import kotlin.math.pow

private const val TAG = "Utils"
var gson: Gson = GsonBuilder().create()
var toast: Toast? = null

val Context.dataStore by dataStore("user_data.json", UserDataSerializer)

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

fun showToast(context: Context, message: String) {
    toast?.cancel()
    toast = Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT)
    toast?.show()
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    if (capabilities != null) {
        when {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> return true
            /*capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true*/
        }
    }
    return false
}

fun String.isEmailValid(): Boolean {
    val expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,8}$"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.containsCapitalLetter(): Boolean {
    val expression = "(?=.*[A-Z]).{1,}"
    val pattern = Pattern.compile(expression, Pattern.UNICODE_CASE)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.containsDigit(): Boolean {
    val expression = "^(?=.*\\d).{1,}$"
    val pattern = Pattern.compile(expression)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.containsSpecialChar(): Boolean {
    val expression = "^(?=.*[!@#&()–[{}]:;',?/*~\$^+=<>]).{1,}$"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.min8Characters(): Boolean {
    val expression = "^.{8,40}$"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun ImageView.loadImage(imageUrl: String?, placeholder: Int) {
    Glide.with(this.context)
        .load(imageUrl)
        .thumbnail(Glide.with(this.context).load(imageUrl))
        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
        .placeholder(placeholder)
        //.transition(DrawableTransitionOptions.withCrossFade(200))
        //.dontAnimate()
        .into(this)
}

fun ImageView.loadImageFromS3(imageUrl: String?, placeholder: Int) {
    val url = S3Utils.generateS3ShareUrl(context, imageUrl)
    Glide.with(this.context)
        .load(url)
        .thumbnail(Glide.with(this.context).load(url))
        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
        .placeholder(placeholder)
        //.transition(DrawableTransitionOptions.withCrossFade(200))
        //.dontAnimate()
        .into(this)
}

fun ImageView.loadCachedImg(
    imgUrl: String?,
    cachedUrl: String?,
    placeholder: Int
): String {
    loadImage(imgUrl,placeholder)
    return imgUrl.toString()
    /*return if (cachedUrl != null) {
        loadImage(cachedUrl, placeholder)
        cachedUrl
    } else {
        val url = S3Utils.generateS3ShareUrl(context, imgUrl)
        loadImage(url, placeholder)
        url
    }*/
}

fun handleErrorResponse(body: ResponseBody?, context: Context, code: Int = 0) {
    if (code == 401) {
//        restartApp(context)
    } else {
        try {
            body?.let {
                val jsonObject = JSONObject(it.string())
                Toast.makeText(context, jsonObject.optString("reason"), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.something_went_wro),
                Toast.LENGTH_LONG
            )
                .show()
            e.printStackTrace()
        }
    }
}

fun ImageView.loadImage(imageUrl: String?, placeholder: Int, pBar: ProgressBar) {
    Glide.with(this.context)
        .load(imageUrl)
        .thumbnail(Glide.with(this.context).load(imageUrl))
        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
        .placeholder(placeholder)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                pBar.remove()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                pBar.remove()
                return false
            }
        })
        .into(this)
}

fun getImageUploadPath(basePath: String): String {
    val dateTime = Calendar.getInstance().time
    val month = SimpleDateFormat("MMM", Locale.ENGLISH)
    val year = SimpleDateFormat("yyyy", Locale.ENGLISH)
    val uuid = UUID.randomUUID().toString().toLowerCase()
    return "$basePath${year.format(dateTime)}/${month.format(dateTime)}/${System.currentTimeMillis()}-$uuid-"
}


fun getImageName(basePath:String,type: String): String {
    return getImageUploadPath(basePath) +type+".jpeg"
}

fun ImageView.toGrayScale() {
    val matrix = ColorMatrix().apply {
        setSaturation(0f)
    }
    colorFilter = ColorMatrixColorFilter(matrix)
}

fun getFormattedNumber(count: Long): String {
    return if (count <= 1000) {
        when (count) {
            in 1..50 -> "$count"
            in 50..100 -> "50+"
            in 100..500 -> "100+"
            in 500..1000 -> "500+"
            else -> "$count"
        }
    } else {
        val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
        String.format("%d%c", count / 1000.0.pow(exp.toDouble()).toInt(), "KMGTPE"[exp - 1]) + "+"
    }
}

fun Button.disableButton() {
    isEnabled = false
    backgroundTintList =
        ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.white))
    //background = ContextCompat.getDrawable(this.context, R.drawable.btn_disabled_shape)
}

fun Button.enableButton() {
    isEnabled = true
    backgroundTintList =
        ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.white))
    //background = ContextCompat.getDrawable(this.context, R.drawable.btn_shape)

}

fun ProgressBar.show(list: ArrayList<*>?) {
    if (list.isNullOrEmpty() && visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun ProgressBar.remove() {
    if (visibility == View.VISIBLE) {
        visibility = View.GONE
    }
}

fun Double.toPriceFormat(): String {
    return DecimalFormat("0.00").format(this)
}

fun Float.toDecimalFormat(): String {
    return DecimalFormat("0.0").format(this)
}

fun convertKmToMiles(kilometers: Double): Double {
    return if (kilometers > 0) {
        kilometers * 0.621
    } else {
        0.0
    }
}

fun getHashKey(context: Context) {
    try {
        val info = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_SIGNATURES
        )
        for (signature in info.signatures) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            Log.e("MY_KEY_HASH:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
        }
    } catch (e: PackageManager.NameNotFoundException) {
    } catch (e: NoSuchAlgorithmException) {
    }
}

fun shareApp(context: Context, title: String?, url: String?) {
    val share = Intent(Intent.ACTION_SEND)
    share.type = "text/plain"
    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    share.putExtra(Intent.EXTRA_SUBJECT, title)
    share.putExtra(Intent.EXTRA_TEXT, url)
    context.startActivity(Intent.createChooser(share, "Share Link:"))
}

fun openPlayStoreLink(context: Context, url: String?) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}


@Throws(UnsupportedEncodingException::class)
fun openEmailClient(
    context: Context, email: String,
    subject: String?, body: String?
) {
    var mSubject = subject
    var mBody = body
    if (mSubject == null) {
        mSubject = ""
    }
    if (mBody == null) {
        mBody = ""
    }
    val uriText = ("mailto:" + email + "?subject="
            + URLEncoder.encode(mSubject, "UTF-8") + "&body="
            + URLEncoder.encode(mBody, "UTF-8"))
    val uri = Uri.parse(uriText)
    val sendIntent = Intent(Intent.ACTION_SENDTO)
    sendIntent.data = uri
    context.startActivity(Intent.createChooser(sendIntent, "Send email"))
}


@SuppressLint("SetJavaScriptEnabled")
fun openWebView(
    webView: WebView,
    url: String?,
    progressBar: ProgressBar
) {
    webView.settings.javaScriptEnabled = true
    webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
    webView.webViewClient = object : WebViewClient() {
        var urll =
            "javascript:(function() {" + "document.querySelector('[role=\"toolbar\"]').remove();})()"

        override fun onPageFinished(webview: WebView, url: String) {
            webView.loadUrl(urll)
            progressBar.visibility = View.GONE
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
        }
    }
    webView.loadUrl(url!!)
}

val String.lastPathComponent: String
    get() {
        var path = this
        if (path.endsWith("/"))
            path = path.substring(0, path.length - 1)
        var index = path.lastIndexOf('/')
        if (index < 0) {
            if (path.endsWith("\\"))
                path = path.substring(0, path.length - 1)
            index = path.lastIndexOf('\\')
            if (index < 0)
                return path
        }
        return path.substring(index + 1)
    }

inline fun <reified T> String.toObject(): T? {
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T> String.toArrayList(): ArrayList<T> {
    return gson.fromJson(this, object : TypeToken<ArrayList<T>>() {}.type)
}

inline fun <reified T> String.toMutableList(): MutableList<T> {
    return if (this.isEmpty()) mutableListOf()
    else gson.fromJson(this, object : TypeToken<MutableList<T>>() {}.type)
}

fun View.show() {
    if (!isVisible) isVisible = true
}

fun View.remove() {
    if (!isGone) isGone = true
}

fun View.hide() {
    if (!isInvisible) isInvisible = true
}

infix fun View.visibleIf(condition: Boolean) =
    run { visibility = if (condition) View.VISIBLE else View.GONE }

infix fun View.goneIf(condition: Boolean) =
    run { visibility = if (condition) View.GONE else View.VISIBLE }

infix fun View.invisibleIf(condition: Boolean) =
    run { visibility = if (condition) View.INVISIBLE else View.VISIBLE }


fun EditText.restrictNameInput() {
    val filter = InputFilter { source, start, end, _, _, _ ->
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ "
        val builder = SpannableStringBuilder()
        for (i in start until end) {
            val c = source?.get(i)
            if (c != null && allowedChars.contains(c)) {
                builder.append(c)
            }
        }
        val allCharactersValid = builder.length == end - start
        if (!allCharactersValid) {
            error = "No Special Characters,Numbers or Symbol allowed."
        }
        if (allCharactersValid) null else builder
    }

    filters = arrayOf(filter)
}

fun EditText.restrictNameAndNumbersInput() {
    val filter = InputFilter { source, start, end, _, _, _ ->
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789. "
        val builder = StringBuilder()
        for (i in start until end) {
            val c = source?.get(i)
            if (c != null && allowedChars.contains(c)) {
                builder.append(c)
            }
        }
        val allCharactersValid = builder.length == end - start
        if (!allCharactersValid) {
            error = "No Special Characters or Symbols allowed."
        }
        if (allCharactersValid) null else builder.toString()
    }

    filters = arrayOf(filter)
}

fun EditText.restrictOrganizationNameInput() {
    val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,./_"
    val filter = InputFilter { source, start, end, _, _, _ ->
        val builder = StringBuilder()
        for (i in start until end) {
            val c = source?.get(i)
            if (c != null && allowedChars.contains(c)) {
                builder.append(c)
            }
        }
        val allCharactersValid = builder.length == end - start
        if (!allCharactersValid) {
            error = "No special characters allowed."
        }
        if (allCharactersValid) null else builder.toString()
    }

    filters = arrayOf(filter)
}

fun setupSpaceAndLengthRestriction(editText: EditText?, minLength: Int, maxLength: Int) {
    editText?.filters = arrayOf(
        InputFilter { source, start, end, dest, dstart, dend ->
            if (dstart == 0 && source == " ") {
                ""
            } else {
                source
            }
        },
        InputFilter.LengthFilter(maxLength)
    )

    editText?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // No action needed before text changes
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // No action needed as text is changing
        }

        override fun afterTextChanged(p0: Editable?) {
            p0?.let {
                val trimmedText = it.toString().trim()
                if (TextUtils.isEmpty(trimmedText) || trimmedText.length < minLength) {
                    editText.error = "Name must be between $minLength and $maxLength characters"
                } else if (trimmedText.length > maxLength) {
                    val trimString = p0.toString().substring(0, maxLength)
                    editText.setText(trimString)
                    editText.setSelection(trimString.length)
                    editText.error = "Name must be between $minLength and $maxLength characters"
                } else {
                    editText.error = null
                }
            }
        }
    })
}