package com.siddhantkushwaha.alfred.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.siddhantkushwaha.alfred.R
import com.siddhantkushwaha.alfred.entity.Notification
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter


class NotificationAdapter(
    private val context: Context,
    data: OrderedRealmCollection<Notification>,
    autoUpdate: Boolean
) : RealmRecyclerViewAdapter<Notification, RecyclerView.ViewHolder>(data, autoUpdate) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(context, view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = data?.get(position) ?: throw Exception("null object not allowed here")
        (holder as NotificationViewHolder).bind(notification)
    }

    private class NotificationViewHolder(val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(notification: Notification) {

            val appLogoImageView = itemView.findViewById<ImageView>(R.id.image_view_app_logo)
            val appNameTextView = itemView.findViewById<TextView>(R.id.text_view_app_name)

            val notificationTitleTextView =
                itemView.findViewById<TextView>(R.id.text_view_notification_title)
            val notificationContentTextView =
                itemView.findViewById<TextView>(R.id.text_view_notification_content)

            val notificationLargeIconImageView =
                itemView.findViewById<ImageView>(R.id.image_view_notification_large_icon)
            val notificationPictureImageView =
                itemView.findViewById<ImageView>(R.id.image_view_notification_picture)

            Glide.with(context).load(notification.getProperty("android.smallIcon")?.lastOrNull())
                .error(R.drawable.icon_logo).into(appLogoImageView)

            appNameTextView.text = notification.appName

            notificationTitleTextView.text = notification.getProperty("android.title")?.lastOrNull()

            val content =
                if (notification.packageName != "com.whatsapp")
                    notification.getProperty("android.text")
                        ?.lastOrNull()
                else
                    notification.getProperty("android.text")
                        ?.takeLast(5)
                        ?.joinToString("\n")

            notificationContentTextView.text = content

            val largeIconUri = notification.getProperty("android.largeIcon")?.lastOrNull()
            if (largeIconUri != null) {
                notificationLargeIconImageView.visibility = View.VISIBLE
                Glide.with(context).load(largeIconUri)
                    .error(R.drawable.icon_logo).circleCrop().into(notificationLargeIconImageView)
            } else {
                notificationLargeIconImageView.visibility = View.GONE
            }

            val picture = notification.getProperty("android.picture")?.lastOrNull()
            if (picture != null) {
                notificationPictureImageView.visibility = View.VISIBLE
                Glide.with(context).load(picture).centerCrop().into(notificationPictureImageView)
            } else {
                notificationPictureImageView.visibility = View.GONE
            }
        }
    }
}