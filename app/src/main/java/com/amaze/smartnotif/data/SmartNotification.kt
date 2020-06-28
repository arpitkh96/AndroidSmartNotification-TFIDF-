package com.amaze.smartnotif.data

import android.os.Parcel
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import com.ort.gop.Data.db.ParsedNotification
import com.ort.gop.Data.db.State

data class NotificationHolder(var key:String, val notification: StatusBarNotification, val parsedNotif: ParsedNotification, val state: State?):Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readParcelable(StatusBarNotification::class.java.classLoader),
            parcel.readParcelable(ParsedNotification::class.java.classLoader),
            parcel.readParcelable(State::class.java.classLoader)){
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeParcelable(notification, flags)
        parcel.writeParcelable(parsedNotif, flags)
        parcel.writeParcelable(state, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotificationHolder> {
        override fun createFromParcel(parcel: Parcel): NotificationHolder {
            return NotificationHolder(parcel)
        }

        override fun newArray(size: Int): Array<NotificationHolder?> {
            return arrayOfNulls(size)
        }
    }

}
data class Event(val event: Int,val key: String,val removedReason:Int,val notificationHolder: NotificationHolder?)
const val EVENT_ADDED_TO_ACTIVE=1
const val EVENT_ADDED_TO_HIDDEN=2
const val EVENT_REMOVED=3

enum class CUSTOM_REASON(val reason: Int){
    FILTERED_BY_APP(25),
    REMOVED_BY_USER_IN_APP(26),
    REMOVED_BY_USER_IN_APP_BULK(27)
}
var reasonStringHolder = mapOf<Int, String>(
        /** Notification was canceled by the status bar reporting a notification click.  */
        1 to "REASON_CLICK",
        /** Notification was canceled by the status bar reporting a user dismissal.  */
        2 to "REASON_CANCEL",
        /** Notification was canceled by the status bar reporting a user dismiss all.  */
        3 to "REASON_CANCEL_ALL",
        /** Notification was canceled by the status bar reporting an inflation error.  */
        4 to "REASON_ERROR",
        /** Notification was canceled by the package manager modifying the package.  */
        5 to "REASON_PACKAGE_CHANGED",
        /** Notification was canceled by the owning user context being stopped.  */
        6 to "REASON_USER_STOPPED",
        /** Notification was canceled by the user banning the package.  */
        7 to "REASON_PACKAGE_BANNED",
        /** Notification was canceled by the app canceling this specific notification.  */
        8 to "REASON_APP_CANCEL",
        /** Notification was canceled by the app cancelling all its notifications.  */
        9 to "REASON_APP_CANCEL_ALL",
        /** Notification was canceled by a listener reporting a user dismissal.  */
        10 to "REASON_LISTENER_CANCEL",
        /** Notification was canceled by a listener reporting a user dismiss all.  */
        11 to "REASON_LISTENER_CANCEL_ALL",
        /** Notification was canceled because it was a member of a canceled group.  */
        12 to "REASON_GROUP_SUMMARY_CANCELED",
        /** Notification was canceled because it was an invisible member of a group.  */
        13 to "REASON_GROUP_OPTIMIZATION",
        /** Notification was canceled by the device administrator suspending the package.  */
        14 to "REASON_PACKAGE_SUSPENDED",
        /** Notification was canceled by the owning managed profile being turned off.  */
        15 to "REASON_PROFILE_TURNED_OFF",
        /** Autobundled summary notification was canceled because its group was unbundled  */
        16 to "REASON_UNAUTOBUNDLED",
        /** Notification was canceled by the user banning the channel.  */
        17 to "REASON_CHANNEL_BANNED",
        /** Notification was snoozed.  */
        18 to "REASON_SNOOZED",
        /** Notification was canceled due to timeout  */
        19 to "REASON_TIMEOUT",

        25 to "FILTERED_BY_APP",
        26 to "REMOVED_BY_USER_IN_APP",
        27 to "REMOVED_BY_USER_IN_APP_BULK"

)
