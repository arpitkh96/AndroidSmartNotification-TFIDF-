package com.amaze.smartnotif.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amaze.smartnotif.NotificationListener
import com.amaze.smartnotif.NotificationListener.Companion.hiddenMap
import com.amaze.smartnotif.activities.AddFilter
import com.amaze.smartnotif.activities.ViewFilters
import com.amaze.smartnotif.adapter.NotificationAdapter
import com.amaze.smartnotif.data.NotificationHolder
import com.amaze.smartnotif.notificationlistenerexample.R
import com.amaze.smartnotif.utils.ItemTouchHelper
import kotlinx.android.synthetic.main.fragment_blocked_notif.*
import kotlinx.android.synthetic.main.fragment_current_notif.*
import kotlinx.android.synthetic.main.fragment_current_notif.list
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.text.clear

class BlockedNotif : Fragment(), NotificationAdapter.Clickcallback {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_blocked_notif, container, false)

    lateinit var adapter: NotificationAdapter
    var localCopy = ArrayList<NotificationHolder>()
    fun loadAsync(context: Context, packageManager: PackageManager) {
        GlobalScope.launch {
            while (!NotificationListener.listenerConnected) delay(200)
            localCopy.clear()
            for (notification in NotificationListener.hiddenMap)
                localCopy.add(notification.value)
            localCopy.sortByDescending { it.parsedNotif.postTime }
            withContext(Dispatchers.Main) {
                list.layoutManager = LinearLayoutManager(context)
                adapter.submitList(localCopy)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { context ->
            list.layoutManager = LinearLayoutManager(context)
            adapter = NotificationAdapter(context, this)
            list.adapter = adapter
        }
        clear.setOnClickListener {
            NotificationListener.hiddenMap.clear()
            context?.let { context->
                activity?.packageManager?.let {
                    loadAsync(context,it)
                }
            }
        }
        val trashBinIcon = resources.getDrawable(
                R.drawable.ic_close_black_24dp,
                null
        )
        val editIcon = resources.getDrawable(
                R.drawable.ic_filter_list_black_24dp,
                null
        )
        val color = Color.parseColor("#eeeeee")
        val myCallback = object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            private var previousDx = 0f
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            fun createSwipeFlags(position: Int): Int {
                if (localCopy[position].notification.isOngoing) {
                    return 0
                }
                return (ItemTouchHelper.START or ItemTouchHelper.END)
            }

            override fun getMovementFlags(recyclerView: RecyclerView,
                                          viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = 0
                return makeMovementFlags(dragFlags, createSwipeFlags(viewHolder.adapterPosition))
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                previousDx = 0f;
                when (direction) {
                    32 -> {
                        try {
                            hiddenMap.remove(localCopy[viewHolder.adapterPosition].key)
                            localCopy.removeAt(viewHolder.adapterPosition)
                            adapter?.submitList(localCopy)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    16 -> {
                        startActivity(Intent(context, ViewFilters::class.java).putExtra("packageName", localCopy[viewHolder.adapterPosition].parsedNotif.packageName).putExtra("showFab", true))
                        list.adapter?.notifyItemChanged(viewHolder.adapterPosition)
                    }
                }
            }

            override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
            ) {

                // More code here

                super.onChildDraw(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive)
                if (previousDx <= 0 && dX > 0) {
                    c.clipRect(0f, viewHolder.itemView.top.toFloat(),
                            dX, viewHolder.itemView.bottom.toFloat())
                    if (dX < recyclerView.width / 3)
                        c.drawColor(color)
                    else
                        c.drawColor(Color.GRAY)
                    val textMargin = resources.getDimension(R.dimen.text_margin)
                            .roundToInt()
                    trashBinIcon.bounds = Rect(
                            textMargin,
                            viewHolder.itemView.top + textMargin,
                            textMargin + trashBinIcon.intrinsicWidth,
                            viewHolder.itemView.top + trashBinIcon.intrinsicHeight
                                    + textMargin
                    )
                    trashBinIcon.draw(c)
                } else if (previousDx >= 0 && dX < 0) {
                    c.clipRect(recyclerView.width.toFloat() - abs(dX), viewHolder.itemView.top.toFloat(),
                            recyclerView.width.toFloat(), viewHolder.itemView.bottom.toFloat())
                    if (abs(dX) < recyclerView.width / 3)
                        c.drawColor(color)
                    else
                        c.drawColor(Color.GRAY)
                    val textMargin = resources.getDimension(R.dimen.text_margin)
                            .roundToInt()
                    editIcon.bounds = Rect(
                            recyclerView.width - (textMargin + trashBinIcon.intrinsicWidth),
                            viewHolder.itemView.top + textMargin,
                            recyclerView.width - textMargin,
                            viewHolder.itemView.top + trashBinIcon.intrinsicHeight
                                    + textMargin
                    )
                    editIcon.draw(c)
                }

            }

        }
        val myHelper = ItemTouchHelper(myCallback)
        myHelper.attachToRecyclerView(list)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            context?.let { context ->
                delay(600)
                withContext(Dispatchers.Main){
                    activity?.packageManager?.let { packageManager -> loadAsync(context, packageManager) }
                }
            }
        }

    }

    override fun onClick(holder: NotificationHolder) {
        holder.notification.notification?.contentIntent?.send()
    }
}