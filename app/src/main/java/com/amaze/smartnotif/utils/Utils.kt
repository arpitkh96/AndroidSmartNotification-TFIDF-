package com.amaze.smartnotif.utils

import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings.Global.DEVICE_NAME
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.collection.LruCache
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amaze.smartnotif.data.NotificationHolder
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import com.ort.gop.Data.db.ParsedNotification
import com.ort.gop.Data.db.State
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection

fun getHolder(sbn: StatusBarNotification, packageManager: PackageManager,notificationManager: NotificationManagerCompat): NotificationHolder {
    var y=sbn.clone()
    val notificationHolder= NotificationHolder(sbn.key, y, ParsedNotification("", "", "", "", "", "", "", "", 0, 0, "", 0), State(sbn.key, false, 0, System.currentTimeMillis()))
    val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(y.packageName, PackageManager.GET_META_DATA)) as String
    notificationHolder.parsedNotif.key=sbn.key
    notificationHolder.parsedNotif.title=y.notification.extras.get("android.title")?.toString()?:""
    notificationHolder.parsedNotif.summary=y.notification.extras.get("android.text")?.toString()?:""
    notificationHolder.parsedNotif.subtext=y.notification.extras.get("android.subText")?.toString()?:""
    notificationHolder.parsedNotif.packageName=y.packageName
    notificationHolder.parsedNotif.category=y.notification.category?:""
    notificationHolder.parsedNotif.postTime=y.postTime
    notificationHolder.parsedNotif.group=y.notification.group?:""
    notificationHolder.parsedNotif.appName=appName
    notificationHolder.parsedNotif.color=y.notification.color
    notificationHolder.parsedNotif.flag=y.notification.flags
    notificationHolder.key=sbn.key
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationHolder.parsedNotif.extra=notificationHolder.parsedNotif.extra+"settingsText:"+y.notification.settingsText?.toString()+"\n"
        notificationHolder.parsedNotif.extra=notificationHolder.parsedNotif.extra+"channelId:"+y.notification.channelId?.toString()+"\n"
        notificationHolder.parsedNotif.extra=notificationHolder.parsedNotif.extra+"groupkey"+y.groupKey+"\n"+"Isgroup"+y.isGroup+"\n"+"Clearable"+y.isClearable+"\n"+"Ongoing"+y.isOngoing+"\n"
        notificationHolder.parsedNotif.extra=notificationHolder.parsedNotif.extra+"extras:"+y.notification.extras?.toString()+"\n"
    }
    return notificationHolder

}
fun isGroupSummary(sbn: StatusBarNotification):Boolean{
    return sbn.groupKey != null && sbn.notification.flags and NotificationCompat.FLAG_GROUP_SUMMARY != 0

}
internal fun ConfigureFuel() {
    FuelManager.instance.basePath = "https://still-springs-88666.herokuapp.com"
    Log.d("Basepath", FuelManager.instance.basePath)
    FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")
}
private fun cleanTextContent(text: String): String {
    var text = text
    // strips off all non-ASCII characters
    text = text.replace("[^\\x00-\\x7F]".toRegex(), "")

    // erases all the ASCII control characters
    text = text.replace("[\\p{Cntrl}&&[^\r\n\t]]".toRegex(), "")

    // removes non-printable characters from Unicode
    text = text.replace("\\p{C}".toRegex(), "")

    return text.trim { it <= ' ' }
}
val cacheSize = 100 * 1024 * 1024; // 100MiB

val cacheStock = object : LruCache<String, Boolean>(cacheSize) {
    override fun sizeOf(key: String, value: Boolean): Int {
        return super.sizeOf(key, value)
    }
}
@Throws
internal fun Reputation(text:String): Boolean {
    cacheStock[text]?.let {
        Log.d("Found in cache",text+" "+it)
        return it }
    val json=JSONObject()
    json.put("email_text",cleanTextContent(text))
    val response = "/api/v1/classify/".httpPost()
            .body(json.toString())
            .timeoutRead(1000)
            .responseString()
    Timber.d("Checkin %d",response.second.statusCode)
    val final_string_res=String(response.second.data)
    when (response.second.statusCode) {
        HttpURLConnection.HTTP_OK -> {
            try {
                val responseObj =
                        JSONObject(final_string_res)
                if (responseObj.has("email_class") && responseObj["email_class"]=="spam"){
                    cacheStock.put(text,true)
                    Log.d("SpamClassifiedFromCLoud",text)
                    return true
                }else{
                    cacheStock.put(text,false)
                }
            } catch (e: Exception) {
                return false
            }

        }
        else->{
            return false
        }
    }
    return false
}


