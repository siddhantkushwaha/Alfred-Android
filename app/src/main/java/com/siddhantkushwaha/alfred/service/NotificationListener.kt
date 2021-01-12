package com.siddhantkushwaha.alfred.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.siddhantkushwaha.alfred.R

class NotificationListener : NotificationListenerService() {

    private val tag = "AlfredNotificationListener"

    private lateinit var notificationServiceReceiver: NotificationServiceReceiver

    override fun onCreate() {
        super.onCreate()

        /* ---- Receiver needed to let app interact with service ----- */

        notificationServiceReceiver = NotificationServiceReceiver()

        val intentFilter = IntentFilter()
        intentFilter.addAction(this.getString(R.string.action_notification_service_receiver))

        registerReceiver(notificationServiceReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationServiceReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn != null) {

            Log.d(tag, "Notification received.")

            // TODO - classify notification and dismiss if spam
            // cancelNotification(sbn.key)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
    }

    internal inner class NotificationServiceReceiver : BroadcastReceiver() {

        private val notificationListener = this@NotificationListener

        override fun onReceive(context: Context?, intent: Intent?) {

            Log.d(tag, "Broadcast received. Showing all active notifications")

            notificationListener.activeNotifications.forEach { notification ->

                Log.d(tag, "${notification.id} ${notification.packageName}")
                val extras = notification.notification.extras
                extras.keySet().forEach { key ->
                    Log.d(tag, "$key, ${extras.get(key)}")
                }
            }

        }
    }
}