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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amaze.smartnotif.NotificationListener
import com.amaze.smartnotif.activities.AddFilter
import com.amaze.smartnotif.adapter.NotificationAdapter
import com.amaze.smartnotif.data.EVENT_ADDED_TO_ACTIVE
import com.amaze.smartnotif.data.EVENT_REMOVED
import com.amaze.smartnotif.data.NotificationHolder
import com.amaze.smartnotif.notificationlistenerexample.R
import com.amaze.smartnotif.utils.ItemTouchHelper
import kotlinx.android.synthetic.main.fragment_current_notif.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class CurrentNotif : Fragment(), NotificationAdapter.Clickcallback {
    lateinit var adapter: NotificationAdapter
    var localCopy=ArrayList<NotificationHolder>()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_current_notif, container, false)

    fun loadAsync(notifications:ArrayList<NotificationHolder>,context: Context,packageManager:PackageManager){
        localCopy=notifications
        adapter.submitList(localCopy)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {context ->
            list.layoutManager=LinearLayoutManager(context)
            adapter=NotificationAdapter(context,this@CurrentNotif)
            list.adapter=adapter
        }
        val trashBinIcon = resources.getDrawable(
                R.drawable.ic_close_black_24dp,
                null
        )
        trashBinIcon.setTint(context?.getColor(R.color.colorFields)?:Color.BLACK)
        val editIcon = resources.getDrawable(
                R.drawable.ic_filter_list_black_24dp,
                null
        )
        editIcon.setTint(context?.getColor(R.color.colorFields)?:Color.BLACK)
        val color=context?.getColor(R.color.colorBg)?:Color.parseColor("#eeeeee")
        val colorHighLighted=context?.getColor(R.color.hightLightColor)?:Color.parseColor("#eeeeee")
        val myCallback = object: ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            private var previousDx = 0f
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            fun createSwipeFlags(position:Int):Int {
                if (localCopy[position].notification.isOngoing){
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
                when(direction){
                    32->{
                        if(localCopy[viewHolder.adapterPosition].notification.isOngoing){
                            clearView(list,viewHolder)
                            return
                        }
                        context?.let {
                            LocalBroadcastManager.getInstance(it).sendBroadcast(Intent("notification-actions").putExtra("action",2).putExtra("id",localCopy[viewHolder.adapterPosition].notification.key))
                        }
                        localCopy.removeAt(viewHolder.adapterPosition)
                        adapter?.submitList(localCopy)
                    }
                    16->{
                        startActivity(Intent(context,AddFilter::class.java).putExtra("notification",localCopy[viewHolder.adapterPosition].parsedNotif))
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
                    if(dX < recyclerView.width / 3)
                        c.drawColor(color)
                    else
                        c.drawColor(colorHighLighted)
                    val textMargin = resources.getDimension(R.dimen.text_margin)
                            .roundToInt()
                    trashBinIcon.bounds = Rect(
                            textMargin,
                            viewHolder.itemView.top + textMargin,
                            textMargin + trashBinIcon.intrinsicWidth,
                            viewHolder.itemView.top + trashBinIcon.intrinsicHeight
                                    + textMargin
                    )
                    trashBinIcon.draw(c)                }
                else if (previousDx >= 0 && dX < 0) {
                    c.clipRect(recyclerView.width.toFloat()-abs(dX), viewHolder.itemView.top.toFloat(),
                            recyclerView.width.toFloat(), viewHolder.itemView.bottom.toFloat())
                    if(abs(dX) < recyclerView.width / 3)
                        c.drawColor(color)
                    else
                        c.drawColor(colorHighLighted)
                    val textMargin = resources.getDimension(R.dimen.text_margin)
                            .roundToInt()
                    editIcon.bounds = Rect(
                            recyclerView.width-(textMargin+trashBinIcon.intrinsicWidth),
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
        NotificationListener.active.observe(this, Observer {
            context?.let {context->
                activity?.packageManager?.let { packageManager -> loadAsync(it,context, packageManager) }
            }
        })
        NotificationListener.event.observe(this, Observer {event->
            when(event.event){
                EVENT_ADDED_TO_ACTIVE->{
                    val index=findIndexOf(event.key)
                    if (index>-1){
                        localCopy.removeAt(index)
                    }
                    localCopy.add(0,event.notificationHolder!!)
                    list.getRecycledViewPool().clear();

                    adapter.submitList(localCopy)
                }
                EVENT_REMOVED->{
                    val index=findIndexOf(event.key)
                    if (index>-1){
                        localCopy.removeAt(index)
                        list.getRecycledViewPool().clear();
                        adapter.submitList(localCopy)
                    }
                }
            }
        })
    }
    fun findIndexOf(key:String):Int{
        localCopy.withIndex().forEach {
            if (it.value.key==key){
                return it.index
            }
        }
        return -1
    }
    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            while (!NotificationListener.listenerConnected) delay(200)
            context?.let {
                delay(600)
                LocalBroadcastManager.getInstance(it).sendBroadcast(Intent("notification-actions").putExtra("action",1))
            }
        }
    }

    override fun onClick(holder: NotificationHolder) {
        holder.notification.notification?.contentIntent?.send()
    }

}