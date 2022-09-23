package com.siddhantkushwaha.alfred.index

import android.content.Context
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.siddhantkushwaha.alfred.common.CommonUtil
import com.siddhantkushwaha.alfred.common.RealmUtil
import com.siddhantkushwaha.alfred.entity.Notification


object Index {
    public fun saveNotification(
        context: Context,
        statusBarNotifications: List<StatusBarNotification>
    ) {
        val task = Thread {
            val realm = RealmUtil.getCustomRealmInstance(context)
            statusBarNotifications.forEach { statusBarNotification ->
                try {
                    realm.executeTransaction { realmT ->
                        val nId = statusBarNotification.id
                        val nTag = statusBarNotification.tag
                        val nPackage = statusBarNotification.packageName

                        val notificationId = CommonUtil.getHash("$nId-$nTag-$nPackage")

                        var notification =
                            realmT.where(Notification::class.java).equalTo("id", notificationId)
                                .findFirst()

                        if (notification == null) {
                            notification =
                                realmT.createObject(Notification::class.java, notificationId)
                                    ?: throw Exception("Could not create notification entity.")

                            notification.packageName = nPackage
                            notification.appName = CommonUtil.getAppNameByPackage(context, nPackage)
                        }

                        var smallIcon =
                            statusBarNotification.notification.smallIcon?.loadDrawable(context)
                                ?.toBitmap()
                        if (smallIcon == null) {
                            smallIcon =
                                CommonUtil.getAppLogoByPackage(context, nPackage)?.toBitmap()
                        }

                        if (smallIcon != null) {
                            val smallIconUri = CommonUtil.saveBitmapToFile(
                                context,
                                smallIcon
                            )
                            notification.appIconUri = smallIconUri
                        }

                        val largeIcon =
                            statusBarNotification.notification.getLargeIcon()?.loadDrawable(context)
                                ?.toBitmap()
                        if (largeIcon != null) {
                            val largeIconUri = CommonUtil.saveBitmapToFile(
                                context,
                                largeIcon
                            )
                            notification.largeIconUri = largeIconUri
                        }

                        val extras = statusBarNotification.notification.extras
                        for (key in extras.keySet()) {
                            val value = extras.get(key) ?: continue
                            Log.d("Index", "$key - [$value]")
                            when (key) {
                                "android.text" -> {
                                    notification.textContent = value.toString()
                                }
                                "android.title" -> {
                                    notification.textTitle = value.toString()
                                }
                                "android.picture" -> {
                                    notification.pictureUri = CommonUtil.getImageUri(context, value)
                                }
                            }
                        }

                        realmT.insertOrUpdate(notification)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            realm.close()
        }
        task.start()
    }

}