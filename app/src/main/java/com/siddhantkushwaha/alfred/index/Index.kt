package com.siddhantkushwaha.alfred.index

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.drawable.Icon
import android.graphics.drawable.PictureDrawable
import android.service.notification.StatusBarNotification
import android.text.SpannedString
import androidx.core.graphics.drawable.toBitmap
import com.siddhantkushwaha.alfred.common.CommonUtil
import com.siddhantkushwaha.alfred.common.RealmUtil
import com.siddhantkushwaha.alfred.entity.Notification


object Index {

    public fun saveNotification(context: Context, sbns: List<StatusBarNotification>) {
        val task = Thread {

            val realm = RealmUtil.getCustomRealmInstance(context)
            sbns.forEach { sbn ->
                realm.executeTransaction { realmT ->
                    val nId = sbn.id
                    val nTag = sbn.tag
                    val nPackage = sbn.packageName

                    val notificationId = CommonUtil.getHash("$nId-$nTag-$nPackage")

                    var notification =
                        realmT.where(Notification::class.java).equalTo("id", notificationId)
                            .findFirst()
                    if (notification == null) {
                        notification = realmT.createObject(Notification::class.java, notificationId)
                            ?: throw Exception("Could not create notification entity.")

                        notification.packageName = nPackage
                        notification.appName = CommonUtil.getAppNameByPackage(context, nPackage)
                        notification.hidden = false
                    }

                    val properties = notification.getProperties() ?: HashMap()

                    val smallIcon = sbn.notification.smallIcon
                    if (smallIcon != null) {
                        val smallIconBitmap = smallIcon.loadDrawable(context).toBitmap()
                        val smallIconUri = CommonUtil.saveBitmapToFile(
                            context,
                            smallIconBitmap
                        )
                        updateValueForKey(
                            sbn,
                            notification,
                            properties,
                            "android.smallIcon",
                            smallIconUri
                        )
                    }

                    val largeIcon = sbn.notification.getLargeIcon()
                    if (largeIcon != null) {
                        val largeIconBitmap = largeIcon.loadDrawable(context).toBitmap()
                        val largeIconUri = CommonUtil.saveBitmapToFile(
                            context,
                            largeIconBitmap
                        )
                        updateValueForKey(
                            sbn,
                            notification,
                            properties,
                            "android.largeIcon",
                            largeIconUri
                        )
                    }

                    val extras = sbn.notification.extras
                    for (key in extras.keySet()) {

                        val value = extras.get(key) ?: continue
                        val valueClass = extras[key]?.javaClass
                        val valueClassString = valueClass.toString()

                        val stringValue = when (valueClass) {

                            String::class.java -> {
                                value.toString()
                            }

                            SpannedString::class.java -> {
                                (value as SpannedString).toString()
                            }

                            Icon::class.java -> {
                                val icon = (value as? Icon)
                                if (icon != null) {
                                    val iconDrawable = icon.loadDrawable(context)
                                    val iconBitmap = iconDrawable.toBitmap()
                                    CommonUtil.saveBitmapToFile(
                                        context,
                                        iconBitmap
                                    )
                                } else null
                            }

                            Bitmap::class.java -> {
                                val bitmap = (value as? Bitmap)
                                if (bitmap != null) {
                                    CommonUtil.saveBitmapToFile(
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
                                    CommonUtil.saveBitmapToFile(
                                        context,
                                        pictureBitmap
                                    )
                                } else null
                            }

                            else -> {
                                value.toString()
                            }
                        }

                        updateValueForKey(sbn, notification, properties, key, stringValue)
                    }

                    notification.setProperties(properties)
                    updateNotificationEntityPostAttributeFetch(notification)

                    realmT.insertOrUpdate(notification)
                }
            }
            realm.close()
        }

        task.start()
    }

    private fun updateValueForKey(
        sbn: StatusBarNotification,
        notification: Notification,
        properties: HashMap<String, ArrayList<String>>,
        key: String,
        value: String?
    ) {
        // For messaging apps (WhatsApp), append
        if (sbn.packageName == "com.whatsapp") {
            val values = properties[key] ?: ArrayList()
            if (values.lastOrNull() != value && value != null) {
                notification.timestamp = sbn.postTime
                values.add(value)
                properties[key] = values
            }
        }

        // simply insert/overwrite
        else if (value != null) {
            // update timestamp when any key updated
            notification.timestamp = sbn.postTime
            properties[key] = arrayListOf(value)
        }
    }

    private fun updateNotificationEntityPostAttributeFetch(notification: Notification) {
        val properties = notification.getProperties() ?: HashMap()
        if (notification.packageName == "com.whatsapp") {
            // these are summary messages from WhatsApp which we don't want to show
            if (!properties.containsKey("android.largeIcon")) {
                notification.hidden = true
                return
            }
        }
    }
}