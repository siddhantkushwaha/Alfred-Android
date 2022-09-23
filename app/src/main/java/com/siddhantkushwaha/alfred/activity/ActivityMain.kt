package com.siddhantkushwaha.alfred.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import com.siddhantkushwaha.alfred.R
import com.siddhantkushwaha.alfred.adapter.NotificationAdapter
import com.siddhantkushwaha.alfred.common.RealmUtil
import com.siddhantkushwaha.alfred.entity.Notification
import com.siddhantkushwaha.alfred.service.NotificationListener
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*


class ActivityMain : ActivityBase() {

    private lateinit var realm: Realm

    private lateinit var notifications: RealmResults<Notification>
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationChangeListener: OrderedRealmCollectionChangeListener<RealmResults<Notification>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationAccess()
        beginService()

        realm = RealmUtil.getCustomRealmInstance(this)

        notifications =
            realm
                .where(Notification::class.java)
                .sort("timestamp", Sort.DESCENDING)
                .findAllAsync()

        notificationChangeListener = OrderedRealmCollectionChangeListener { _, _ ->
            notificationAdapter.notifyDataSetChanged()
        }
        notificationAdapter = NotificationAdapter(this, notifications, true)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = false

        recycler_view_notifications.layoutManager = layoutManager
        recycler_view_notifications.adapter = notificationAdapter

        image_view_logo.setOnClickListener {
            sendSaveBroadcast()
        }

        image_view_logo.setOnLongClickListener {
            RealmUtil.clearData(realm)
            true
        }
    }

    override fun onResume() {
        super.onResume()

        notificationAdapter.notifyDataSetChanged()
        notifications.addChangeListener(notificationChangeListener)
    }

    override fun onPause() {
        super.onPause()

        notifications.removeAllChangeListeners()
    }

    private fun isListenerEnabled(): Boolean {
        val cn = ComponentName(this, NotificationListener::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun requestNotificationAccess() {

        if (isListenerEnabled())
            return

        val intent = Intent()
        intent.action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        startActivityForResult(intent, 5) {
            beginService()
        }
    }

    private fun beginService() {
        val serviceIntent = Intent(this, NotificationListener::class.java)

        // stop older service
        stopService(serviceIntent)

        // run new service
        startService(serviceIntent)
    }

    private fun sendSaveBroadcast() {
        val intent = Intent(getString(R.string.action_notification_service_receiver_fetch))
        sendBroadcast(intent)
    }

    private fun sendCancelBroadcast(key:String) {
        val intent = Intent(getString(R.string.action_notification_service_receiver_cancel))
        intent.putExtra("notification_key", key)
        sendBroadcast(intent)
    }
}