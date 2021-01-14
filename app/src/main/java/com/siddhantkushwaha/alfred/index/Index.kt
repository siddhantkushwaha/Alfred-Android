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

            println("***** ${sbns.size}")

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
                    }

                    notification.packageName = nPackage
                    notification.timestamp = sbn.postTime

                    val smallIcon = sbn.notification.smallIcon
                    if (smallIcon != null) {
                        val smallIconBitmap = smallIcon.loadDrawable(context).toBitmap()
                        val smallIconUri = CommonUtil.saveBitmapToFile(
                            context,
                            "${notificationId}_android.smallIcon.png",
                            smallIconBitmap
                        )
                    }

                    val largeIcon = sbn.notification.getLargeIcon()
                    if (largeIcon != null) {
                        val largeIconBitmap = largeIcon.loadDrawable(context).toBitmap()
                        val largeIconUri = CommonUtil.saveBitmapToFile(
                            context,
                            "${notificationId}_android.largeIcon.png",
                            largeIconBitmap
                        )
                    }

                    val properties = notification.getProperties()
                        ?: HashMap()

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
                                val icon = (value as Icon)
                                val iconDrawable = icon.loadDrawable(context)
                                val iconBitmap = iconDrawable.toBitmap()
                                CommonUtil.saveBitmapToFile(
                                    context,
                                    "${notificationId}_$key.png",
                                    iconBitmap
                                )
                            }

                            Bitmap::class.java -> {
                                val bitmap = (value as Bitmap)
                                CommonUtil.saveBitmapToFile(
                                    context,
                                    "${notificationId}_$key.png",
                                    bitmap
                                )
                            }

                            Picture::class.java -> {
                                val picture = (value as Picture)
                                val pictureDrawable = PictureDrawable(picture)
                                val pictureBitmap = pictureDrawable.toBitmap()
                                CommonUtil.saveBitmapToFile(
                                    context,
                                    "${notificationId}_$key.png",
                                    pictureBitmap
                                )
                            }

                            else -> {
                                value.toString()
                            }

                        }

                        properties[key] = Pair(valueClassString, stringValue)
                    }

                    notification.setProperties(properties)
                    realmT.insertOrUpdate(notification)
                }
            }
            realm.close()
        }

        task.start()
    }
}