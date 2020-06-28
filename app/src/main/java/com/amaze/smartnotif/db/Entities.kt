package com.ort.gop.Data.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ParsedNotification(@PrimaryKey var key: String,
                              var appName: String,
                              var title: String,
                              var summary: String,
                              var subtext: String,
                              var packageName: String,
                              var category: String,
                              var group: String,
                              var postTime: Long,
                              var color: Int,
                              var extra: String,
                              var flag: Int):Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(appName)
        parcel.writeString(title)
        parcel.writeString(summary)
        parcel.writeString(subtext)
        parcel.writeString(packageName)
        parcel.writeString(category)
        parcel.writeString(group)
        parcel.writeLong(postTime)
        parcel.writeInt(color)
        parcel.writeString(extra)
        parcel.writeInt(flag)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParsedNotification> {
        override fun createFromParcel(parcel: Parcel): ParsedNotification {
            return ParsedNotification(parcel)
        }

        override fun newArray(size: Int): Array<ParsedNotification?> {
            return arrayOfNulls(size)
        }
    }

}

@Entity
data class State(var key: String,
                 var removed: Boolean, var removedReason: Int, var time: Long) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(key)
        dest?.writeByte(if (removed) 1 else 0)
        dest?.writeInt(removedReason)
        dest?.writeLong(time)
        dest?.writeInt(id)
    }


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readLong()) {
        id = parcel.readInt()
    }

    companion object CREATOR : Parcelable.Creator<State> {
        override fun createFromParcel(parcel: Parcel): State {
            return State(parcel)
        }

        override fun newArray(size: Int): Array<State?> {
            return arrayOfNulls(size)
        }
    }
}

@Entity
data class Word(var word:String,var df:Int){
    @PrimaryKey(autoGenerate = true) var id:Long=0
}
@Entity
data class ExemptedApp(@PrimaryKey var packageName: String)

@Entity
data class Filter(var column: String, val op: Int, val stringval: String, val intval: Int) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
