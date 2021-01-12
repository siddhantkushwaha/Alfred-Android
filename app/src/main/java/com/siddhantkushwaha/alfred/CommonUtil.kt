package com.siddhantkushwaha.alfred

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
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
            .fold("", { str, it -> str + "%02x".format(it) })
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
}