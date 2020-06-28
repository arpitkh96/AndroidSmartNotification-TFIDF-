package com.amaze.smartnotif.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amaze.smartnotif.data.NotificationHolder
import com.amaze.smartnotif.notificationlistenerexample.R
import com.amaze.smartnotif.notificationlistenerexample.databinding.RowNotificationBinding



class NotificationAdapter(val context: Context, val clickcallback: Clickcallback) : ListAdapter<NotificationHolder,NotificationAdapter.GenericViewHolder>(NotificationDC()) {
    var color_grey: Int

    init {

        color_grey = Color.parseColor("#666666")
    }

    interface Clickcallback {
        fun onClick(user: NotificationHolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout.row_notification, parent, false)
        return GenericViewHolder(binding)
    }
    override fun submitList(list: List<NotificationHolder>?) {
        super.submitList(if (list != null) ArrayList(list) else null)
    }
    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }


    inner class GenericViewHolder(val binding: ViewDataBinding) :
            RecyclerView.ViewHolder(binding.getRoot()) {
        fun bind(notification: NotificationHolder) {
            binding as RowNotificationBinding
            binding.title.text = notification.parsedNotif.title
            binding.summary.text = notification.parsedNotif.summary
            binding.icon.setImageDrawable(notification.notification.notification.smallIcon.loadDrawable(context))
            binding.appName.text = notification.parsedNotif.appName
            if (notification.parsedNotif.color != 0) {
                binding.icon.setColorFilter(notification.parsedNotif.color, android.graphics.PorterDuff.Mode.MULTIPLY)
                binding.appName.setTextColor(notification.parsedNotif.color)
            } else {
                binding.icon.setColorFilter(color_grey, android.graphics.PorterDuff.Mode.MULTIPLY)
                binding.appName.setTextColor(color_grey)
            }
            binding.smallTitle.text = notification.parsedNotif.subtext
            binding.root.setOnClickListener {
                clickcallback.onClick(notification)
            }
        }

    }

    private class NotificationDC : DiffUtil.ItemCallback<NotificationHolder>() {
        override fun areItemsTheSame(
                oldItem: NotificationHolder,
                newItem: NotificationHolder
        ) = oldItem.key == newItem.key

        override fun areContentsTheSame(
                oldItem: NotificationHolder,
                newItem: NotificationHolder
        ) = oldItem.parsedNotif == newItem.parsedNotif
    }
}