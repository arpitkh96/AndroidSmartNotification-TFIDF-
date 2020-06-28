package com.amaze.smartnotif.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.amaze.smartnotif.NotificationListener
import com.amaze.smartnotif.fragments.BlockedNotif
import com.amaze.smartnotif.fragments.CurrentNotif
import com.amaze.smartnotif.notificationlistenerexample.R
import com.ort.gop.Data.db.AppDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity(){
    public var mService: NotificationListener?=null
    public var mBound: Boolean = false
    private var enableNotificationListenerAlertDialog: AlertDialog? = null


    override  fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.amaze.smartnotif.notificationlistenerexample.R.layout.activity_main)
        setSupportActionBar(toolbar)
        fragment_holder.adapter=ScreenSlidePagerAdapter(supportFragmentManager)
        tabs.setupWithViewPager(fragment_holder)
        val tabIndex=intent.getIntExtra("tab",0)
        fragment_holder.setCurrentItem(tabIndex)
        fragment_holder.setPagingEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_filter,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_export->{
                exportDb()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = 2
        override fun getPageTitle(position: Int): CharSequence? {
            return when (position){
                0->"ACTIVE"
                else->"HIDDEN"
            }
        }

        override fun getItem(position: Int): Fragment{
            return when (position){
                    0->CurrentNotif()
                    else->BlockedNotif()
                }
        }
    }
    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(com.amaze.smartnotif.notificationlistenerexample.R.string.notification_listener_service)
        alertDialogBuilder.setMessage(com.amaze.smartnotif.notificationlistenerexample.R.string.notification_listener_service_explanation)
        alertDialogBuilder.setPositiveButton(com.amaze.smartnotif.notificationlistenerexample.R.string.yes
        ) { dialog, id -> startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        alertDialogBuilder.setNegativeButton(com.amaze.smartnotif.notificationlistenerexample.R.string.no
        ) { dialog, id ->
            finish()
            // If you choose to not enable the notification listener
            // the app. will not work as expected
        }
        return alertDialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()
        // If the user did not turn the notification listener service on we prompt him to do so
        if (!permissionCheck()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog()
            enableNotificationListenerAlertDialog!!.show()
        }
    }
    fun permissionCheck():Boolean{
        val contentResolver = getContentResolver();
        val enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        val packageName = getPackageName();

// check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
        {
            // in this situation we know that the user has not granted the app the Notification access permission
            return false
        }
        else
            return true
    }
    override fun onStart() {
        super.onStart()
        // Bind to NotificationListener
        /*GlobalScope.launch {
            while (!NotificationListener.listenerConnected)kotlinx.coroutines.delay(200)
            Intent(this@MainActivity, NotificationListener::class.java).also { intent ->
                //bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }*/
    }
    override fun onStop() {
        super.onStop()
        //unbindService(connection)
        mBound = false
    }

    fun exportDb(){
        GlobalScope.async {
            AppDatabase.flush()
            val dir= File(filesDir.parentFile.absolutePath,"databases")
            dir.copyRecursively(
                    getExternalFilesDir(null),
                    true,
                    onError ={ file: File, ioException: IOException ->
                        ioException.printStackTrace()
                        OnErrorAction.SKIP
                    }
            )
            runOnUiThread{
                Toast.makeText(this@MainActivity,"Done",Toast.LENGTH_SHORT).show()
            }
        }
    }
    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as NotificationListener.MyBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
    override fun onPause() {
        super.onPause()

    }
    companion object {
        private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        private val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    }
}
