package com.siddhantkushwaha.alfred.common

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.PictureDrawable
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


object CommonUtil {
    public fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    public fun getHash(data: String, algorithm: String = "SHA-256"): String {
        return MessageDigest.getInstance(algorithm).digest(data.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    public fun formatTimestamp(timestamp: Long, format: String): String {
        val timeZoneId = TimeZone.getDefault().toZoneId()
        val date = Instant.ofEpochMilli(timestamp).atZone(timeZoneId)
        return DateTimeFormatter.ofPattern(format).format(date)
    }

    public fun getStringForTimestamp(timestamp: Long): String {
        val currTimestamp = Instant.now().toEpochMilli()

        return when ((currTimestamp - timestamp) / (24 * 3600000)) {
            0L -> {
                formatTimestamp(timestamp, "hh:mm a")
            }

            1L -> {
                "Yesterday"
            }

            2L, 3L, 4L, 5L, 6L, 7L -> {
                formatTimestamp(timestamp, "EEE")
            }

            else -> {
                formatTimestamp(timestamp, "dd/MM/yy")
            }
        }
    }

    public fun checkPermissions(context: Context, permissions: Array<String>): Array<String> {
        return permissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }

    public fun saveBitmapToFile(context: Context, bitmap: Bitmap): String? {
        try {
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)

            val bitmapBytes = bos.toByteArray()
            bos.close()

            val hash = getHash(String(bitmapBytes))
            val name = "${hash}.png"

            val file = File(context.getExternalFilesDir(null), name)
            file.writeBytes(bitmapBytes)
            return file.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    public fun getAppNameByPackage(context: Context, packageName: String): String? {
        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            return context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    public fun getAppLogoByPackage(context: Context, packageName: String): Drawable? {
        try {
            return context.packageManager.getApplicationLogo(packageName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    public fun getImageUri(context: Context, value: Any): String? {
        return when (value.javaClass) {
            Icon::class.java -> {
                val icon = (value as? Icon)
                if (icon != null) {
                    val iconDrawable = icon.loadDrawable(context)
                    val iconBitmap = iconDrawable.toBitmap()
                    saveBitmapToFile(
                        context,
                        iconBitmap
                    )
                } else null
            }

            Bitmap::class.java -> {
                val bitmap = (value as? Bitmap)
                if (bitmap != null) {
                    saveBitmapToFile(
                        context,
                        bitmap
                    )
                } else null
            }

            Picture::class.java -> {
                val picture = (value as? Picture)
                if (picture != null) {
                    val pictureDrawable = PictureDrawable(picture)
                    val pictureBitmap = pictureDrawable.toBitmap()
                    saveBitmapToFile(
                        context,
                        pictureBitmap
                    )
                } else null
            }

            else -> {
                null
            }
        }
    }
}