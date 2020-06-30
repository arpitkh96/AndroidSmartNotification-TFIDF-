package com.amaze.smartnotif

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.companion.CompanionDeviceManager
import android.content.*
import android.os.Build
import android.os.UserHandle
import android.preference.PreferenceManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.ArrayMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amaze.smartnotif.activities.MainActivity
import com.amaze.smartnotif.data.*
import com.amaze.smartnotif.data.tfidf.Tfidfworker
import com.amaze.smartnotif.notificationlistenerexample.R
import com.amaze.smartnotif.utils.ConfigureFuel
import com.amaze.smartnotif.utils.getHolder
import com.amaze.smartnotif.utils.isGroupSummary
import com.ort.gop.Data.db.AppDatabase
import com.ort.gop.Data.db.State
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.StringBuilder

class NotificationListener : NotificationListenerService() {
    var receiver = Reciever()
    lateinit var notiManager: NotificationManagerCompat
    val CHANNEL_ID = "main"
    val CHANNEL_ID_UPDATES = "updates"
    var sharedPreferences: SharedPreferences? = null
    val routineScope=CoroutineScope(Dispatchers.Default)
    lateinit var broadcastReciever:MyBroadcastReceiver
    override fun onCreate() {
        super.onCreate()
        initRules()
        ConfigureFuel()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        loadRules(sharedPreferences!!)
        notiManager = NotificationManagerCompat.from(this)
        System.out.println("onstart")
        broadcastReciever=MyBroadcastReceiver(this@NotificationListener)
        createNotificationChannel()
        GlobalScope.launch {
            val worker= Tfidfworker.getInstance(this@NotificationListener)
        }

    }
    class MyBroadcastReceiver(internal var caller: NotificationListener) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.getStringExtra("action")?:"";
            if (action.equals("START")) {
                caller.routineScope.launch {
                    kotlinx.coroutines.delay(1000)
                    caller.newTimer()
                }
            }

        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ("Default")
            val descriptionText = ("Only one notficiation")
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID_UPDATES,"Updates",NotificationManager.IMPORTANCE_HIGH).apply {
                description="Updates like rest timer"
            })
        }
    }

    fun publishNotification(number: Int, description: String) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("tab", 1)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentInfo(description)
                .setContentTitle("$number Notifications hidden")
                .setContentText(description)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

    inner class MyBinder : android.os.Binder() {
        val service: NotificationListener
            get() = this@NotificationListener
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("notification-actions"))
        listenerConnected = true
        System.out.println("onListenerConnected")
        registerReceiver(broadcastReciever, IntentFilter("notification-actions-external"))
        routineScope.launch {
            newTimer()
        }
    }
    suspend fun newTimer(){
        with(NotificationManagerCompat.from(this@NotificationListener)) {
            // notificationId is a unique int for each notification that you must define
            cancel(2)
        }
        System.out.println("timer started")
        delay(20*60*1000)
        System.out.println("timer ticked")
        withContext(Dispatchers.Main){
            val builder = NotificationCompat.Builder(this@NotificationListener, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Take 1 minute rest")
                    .setContentText("")
                    // Set the intent that will fire when the user taps the notification
                    .setChannelId(CHANNEL_ID_UPDATES)
                    .setOngoing(false)
            val intent = Intent("notification-actions-external");
            intent.putExtra("action", "START");

            builder.addAction(
                    NotificationCompat.Action(
                            R.mipmap.ic_launcher,
                            "Start Now",
                            PendingIntent.getBroadcast(
                                    applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    )
            )
            with(NotificationManagerCompat.from(this@NotificationListener)) {
                // notificationId is a unique int for each notification that you must define
                notify(2, builder.build())
            }
        }
    }
    inner class Reciever : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            System.out.println("GOT event")
            val action = intent?.getIntExtra("action", 0) ?: 0
            when (action) {
                1 -> {
                    if (!listenerConnected) return

                    val notifications = ArrayList<NotificationHolder>()
                    val routine = GlobalScope.async {

                        val instance = AppDatabase.getInstance(this@NotificationListener)
                        for (x in activeNotifications) {
                            if (x.packageName == packageName) continue
                            if (isGroupSummary(x)) continue
                            var notificationHolder = getHolder(x, packageManager, notiManager)
                            if (!x.isOngoing && filter(notificationHolder.parsedNotif)) {
                                try {
                                    cancelNotification(notificationHolder.key)
                                    instance.stateDao().insert(State(notificationHolder.key, true, CUSTOM_REASON.FILTERED_BY_APP.reason, 0))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                hiddenMap[x.key] = notificationHolder
                            } else {
                                notifications.add(notificationHolder)
                            }
                        }
                    }
                    runBlocking {
                        routine.await()
                    }
                    publishNotification(hiddenMap.size, getNotificationDescription())

                    active.postValue(notifications)
                }
                2 -> {
                    intent?.getStringExtra("id")?.let {
                        if (listenerConnected) {
                            cancelNotification(it)

                            var x = GlobalScope.async {
                                val instance = AppDatabase.getInstance(this@NotificationListener)
                                instance.stateDao().insert(State(it, true, CUSTOM_REASON.REMOVED_BY_USER_IN_APP.reason, 0))
                            }
                        }

                    }
                }
            }

        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        listenerConnected = false
        routineScope.cancel()
        unregisterReceiver(broadcastReciever)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        System.out.println("onListenerDisconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        var y = sbn.clone()
        System.out.println(y)
        System.out.println(y.notification.extras)
        if (isGroupSummary(sbn)) return
        var notificationHolder = getHolder(sbn, packageManager, notiManager)
        val instance = AppDatabase.getInstance(this)
        var inserter = GlobalScope.async {
            try {
                instance.notificationDao().insert(notificationHolder.parsedNotif)
                notificationHolder.state?.let {
                    instance.stateDao().insert(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!sbn.isOngoing && filter(notificationHolder.parsedNotif)) {
            try {
                cancelNotification(sbn.key)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            hiddenMap[sbn.key] = notificationHolder
            event.postValue(Event(EVENT_ADDED_TO_HIDDEN, sbn.key, 0, notificationHolder))
            publishNotification(hiddenMap.size, getNotificationDescription())
        } else event.postValue(Event(EVENT_ADDED_TO_ACTIVE, sbn.key, 0, notificationHolder))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap, reason: Int) {
        if (sbn.packageName == packageName) return
        var x = GlobalScope.async {
            try {
                val instance = AppDatabase.getInstance(this@NotificationListener)
                instance.stateDao().insert(State(sbn.key, true, reason, 0))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        event.postValue(Event(EVENT_REMOVED, sbn.key, reason, null))
    }

    fun getNotificationDescription(): String {
        val appMap = hashMapOf<String, Int>()
        val appNameMap = hashMapOf<String, String>()
        for (x in hiddenMap) {
            val packageName = x.value.notification.packageName
            appMap[packageName] = appMap[packageName]?.plus(1) ?: 1
            appNameMap[packageName] = x.value.parsedNotif.appName
        }
        val array = arrayListOf<String>()
        for (x in appMap) {
            array.add(appNameMap[x.key] + ": " + x.value.toString())
        }
        array.sortBy { it }
        val stringSummary = StringBuilder()
        for (x in array) stringSummary.appendln(x)
        return stringSummary.toString()
    }

    companion object {
        var hiddenMap = ArrayMap<String, NotificationHolder>()
        var active = MutableLiveData<ArrayList<NotificationHolder>>()
        var event = MutableLiveData<Event>()
        var listenerConnected = false
    }
}
