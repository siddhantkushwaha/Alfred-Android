package com.siddhantkushwaha.alfred.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.siddhantkushwaha.alfred.R
import com.siddhantkushwaha.alfred.index.Index


class NotificationListener : NotificationListenerService() {

    private val tag = "AlfredNotificationListener"

    private lateinit var notificationServiceReceiver: NotificationServiceReceiver

    override fun onCreate() {
        super.onCreate()
        /* ---- Receiver needed to let app interact with service ----- */

        notificationServiceReceiver = NotificationServiceReceiver()

        val intentFilter = IntentFilter()
        intentFilter.addAction(this.getString(R.string.action_notification_service_receiver_fetch))

        registerReceiver(notificationServiceReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationServiceReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn != null) {
            // this function will be called multiple times for ongoing notifications
            // and drain all the battery, do NOT record them
            // they CAN be fetched during fetching all active notifications
            if (sbn.isOngoing)
                return

            Log.d(tag, "Notification received, saving.")

            Index.processNotifications(this, listOf(sbn)) { notificationKey, notificationType ->
                when (notificationType) {
                    "spam" -> {
                        cancelNotification(notificationKey)
                    }
                }
            }
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
            if (context == null || intent == null) return
            val action = intent.action ?: return
            when (action) {
                getString(R.string.action_notification_service_receiver_fetch) -> {
                    Log.d(tag, "Broadcast action received. Saving all active notifications.")
                    Index.processNotifications(
                        context,
                        notificationListener.activeNotifications.asList()
                    ) { notificationKey, notificationType ->

                    }
                }
                getString(R.string.action_notification_service_receiver_cancel) -> {
                    Log.d(tag, "Broadcast action received. Cancel active notification.")

                    val notificationKey = intent.getStringExtra("notification_key")
                    notificationListener.cancelNotification(notificationKey)
                }
            }
        }
    }
}