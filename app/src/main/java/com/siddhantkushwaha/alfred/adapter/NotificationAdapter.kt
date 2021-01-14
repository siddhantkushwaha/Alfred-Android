package com.siddhantkushwaha.alfred.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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


        }
    }
}