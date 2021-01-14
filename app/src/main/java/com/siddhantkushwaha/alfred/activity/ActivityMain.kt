package com.siddhantkushwaha.alfred.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.siddhantkushwaha.alfred.R
import com.siddhantkushwaha.alfred.common.RealmUtil
import com.siddhantkushwaha.alfred.service.NotificationListener
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*


class ActivityMain : ActivityBase() {

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realm = RealmUtil.getCustomRealmInstance(this)

        requestNotificationAccess()

        beginService()

        image_view_logo.setOnClickListener {
            sendBroadcast()
        }

        image_view_logo.setOnLongClickListener {
            RealmUtil.clearData(realm)
            true
        }
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

    private fun sendBroadcast() {
        val intent = Intent(getString(R.string.action_notification_service_receiver))
        sendBroadcast(intent)
    }
}