package com.gigzz.android.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.amazonaws.util.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

fun createVideoThumb(context: Context, uri: Uri): Bitmap? {
    try {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, uri)
        return mediaMetadataRetriever.frameAtTime
    } catch (ex: Exception) {
        Log.d("VideoThumbnailError", "createVideoThumb: Error retrieving bitmap")
    }
    return null
}

fun Bitmap.convertToFile(context: Context): File {
//    val tempFile = File.createTempFile("temp" + mediaList.size, ".png")
//    val bytes = ByteArrayOutputStream()
    val f = File(context.cacheDir, "gizz_post_thumbnail")
    f.createNewFile()
    val bos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bos)
    val bitmapdata = bos.toByteArray()
    val fos = FileOutputStream(f)
    fos.write(bitmapdata)
    fos.flush()
    fos.close()
    return f
}

fun Context.getAbsolutePathFromUri(contentUri: Uri): String? {
    var cursor: Cursor? = null
    return try {
        cursor = this
            .contentResolver
            .query(contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        if (cursor == null) {
            return null
        }
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        cursor.getString(columnIndex)
    } catch (e: RuntimeException) {
        Log.e(
            "VideoViewerFragment", String.format(
                "Failed in getting absolute path for Uri %s with Exception %s",
                contentUri.toString(), e.toString()
            )
        )
        null
    } finally {
        cursor?.close()
    }
}

fun getFileName(context: Context?, fileUri: Uri?): String? {
    var name = ""
    val cursor = context?.contentResolver?.query(fileUri!!, null, null, null, null)
    if (cursor != null) {
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        name = cursor.getString(index)
        cursor.close()
    }
    return name
}

fun getRealPathFromUri(context: Context, contentUri: Uri): String {

    var file: File? = null
    try {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(contentUri, "r", null)
        val inputStream: InputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        file = File(context.cacheDir, getFileName(context, contentUri))
        val fileOutputStream: OutputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, fileOutputStream)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }

    return file?.path.toString()
}

fun saveBitmapToFile(file: File): File? {
    try {
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 6

        var inputStream: FileInputStream? = FileInputStream(file)
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream!!.close()

        val originalWidth = options.outWidth
        val originalHeight = options.outHeight

        if (originalWidth > 0) {
            val reqWidth = 200 //720
            var reqHeight = reqWidth * originalHeight / originalWidth
            if (reqHeight >= 200)
                reqHeight = 200 //1280
            Log.d("new image ", "getDropboxIMGSize: $reqHeight    $reqWidth")
            inputStream = FileInputStream(file)
            options = BitmapFactory.Options()

            options.inSampleSize =
                Math.max(originalWidth / reqWidth, originalHeight / reqHeight)
            val roughBitmap = BitmapFactory.decodeStream(inputStream, null, options)

            val m = Matrix()
            val inRect = RectF(0f, 0f, roughBitmap!!.width.toFloat(), roughBitmap.height.toFloat())
            val outRect = RectF(0f, 0f, reqWidth.toFloat(), reqHeight.toFloat())
            m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER)
            val values = FloatArray(9)
            m.getValues(values)

            val resizedBitmap = Bitmap.createScaledBitmap(
                roughBitmap,
                (roughBitmap.width * values[0]).toInt(),
                (roughBitmap.height * values[4]).toInt(), true
            )

            file.createNewFile()
            val out = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
        return file

    } catch (e: IOException) {
        Log.e("Image", e.message, e)
        return null
    }
}


fun getMimeType(url: String): String? {
    var type: String? = null
    val extension =
        url.substring(url.lastIndexOf(".") + 1) /*MimeTypeMap.getFileExtensionFromUrl(BASE_URL);*/
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}