package com.amaze.smartnotif.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.parseAsHtml
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.amaze.smartnotif.data.FilterUI
import com.amaze.smartnotif.data.NotificationHolder
import com.amaze.smartnotif.notificationlistenerexample.R
import com.amaze.smartnotif.notificationlistenerexample.databinding.RowFilterBinding


class FilterAdapter(val context: Context, val clickcallback: Clickcallback,val list: ArrayList<FilterUI>) : RecyclerView.Adapter<FilterAdapter.GenericViewHolder>() {

    var color_grey: Int

    init {

        color_grey = Color.parseColor("#666666")
    }

    interface Clickcallback {
        fun onClick(user: FilterUI)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout.row_filter, parent, false)
        return GenericViewHolder(binding)
    }
    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class GenericViewHolder(val binding: ViewDataBinding) :
            RecyclerView.ViewHolder(binding.getRoot()) {
        fun bind(filter: FilterUI) {
            binding as RowFilterBinding
            if (filter.title!="*")
                binding.title.text="<b>Title: </b> %s".format(filter.title).parseAsHtml()
            else binding.title.text="<b>Title: </b> ".parseAsHtml()
            if (filter.subTitle!="*")
                binding.subtitle.text="<b>Subtitle: </b> %s".format(filter.subTitle).parseAsHtml()
            else binding.subtitle.text="<b>Subtitle: </b>".parseAsHtml()
            if (filter.summmary!="*")
                binding.summary.text="<b>Summary: </b> %s".format(filter.summmary).parseAsHtml()
            else binding.summary.text="<b>Summary: </b> ".parseAsHtml()
            binding.root.setOnClickListener {
                clickcallback.onClick(filter)
            }
        }

    }


}