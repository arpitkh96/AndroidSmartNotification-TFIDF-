package com.amaze.smartnotif.data

import android.content.SharedPreferences
import androidx.collection.ArrayMap
import com.amaze.smartnotif.utils.Reputation
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.ort.gop.Data.db.ParsedNotification
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

var packageFilter :FilterObj?=null

const val NULL_OP = 0;
const val STRING_OP_CONTAINS = 1;
const val STRING_OP_EQUALS = 2;
const val STRING_OP_CONTAINS_IGNORECASE = 3;
const val INT_OP_EQUALS = 11;
const val INT_OP_GREATER = 12;
const val INT_OP_LESS = 13;
const val INT_OP_GREATEROREQUAL = 14;
const val INT_OP_LESSOREQUAL = 15;
const val INT_OP_BITWISEOR = 16;
const val INT_OP_BITWISEAND = 17;
data class FilterUI(var title:String,var subTitle:String,var summmary:String)
public fun <K, V> arrayMapOf(vararg pairs: Pair<K, V>): ArrayMap<K, V> {
        val map=ArrayMap<K,V>()
        if (pairs.size > 0){
            pairs.forEach {
                map[it.first]=it.second
            }
        }
    return map
}

fun initRules(){
    val endObj=FilterObj(true,true)
//    val whiteendObj=FilterObj(true,false)
    val packageList=ArrayMap<String,FilterObj>()
   /* packageList["com.nis.app"]= arrayListOf(endObj)
    packageList["com.google.android.apps.nbu.paisa.user"]=arrayListOf(endObj)
    packageList["com.google.android.apps.magazines"]=arrayListOf(endObj)
    packageList["com.google.android.apps.maps"]=arrayListOf(endObj)
    packageList["com.activision.callofduty.shooter"]=arrayListOf(endObj)
    packageList["com.google.android.googlequicksearchbox"]=arrayListOf(endObj)
    packageList["com.google.android.apps.dynamite"]= arrayListOf(FilterObj("title", STRING_OP_CONTAINS_IGNORECASE,false, arrayMapOf("build-away" to arrayListOf(endObj))))
    packageList["com.google.android.gm"]= arrayListOf(FilterObj("subtext", STRING_OP_EQUALS,false, arrayMapOf("khuranaarpit96@gmail.com" to arrayListOf(endObj))))
    packageList["com.atlassian.android.jira.core"]= arrayListOf(FilterObj("title", STRING_OP_CONTAINS_IGNORECASE,false, arrayMapOf("pranav" to arrayListOf(endObj),"indira" to arrayListOf(endObj),"kala" to arrayListOf(endObj))))
    packageList["org.telegram.messenger"]= arrayListOf(FilterObj("summary",STRING_OP_CONTAINS_IGNORECASE,false, arrayMapOf("joined telegram" to arrayListOf(endObj))))
   */
    packageFilter=FilterObj("packageName",STRING_OP_EQUALS,false, packageList)
    println(Gson().toJson(packageFilter))
    //filters.add(packageFilter)
}
fun blacklist(packageName :String,sharedPreferences: SharedPreferences?){
    packageFilter?.let { packageFilter ->
        sharedPreferences?.let { sharedPreferences ->
            val endObj=FilterObj(true,true)
            packageFilter.values.put(packageName, endObj)

            saveRules(sharedPreferences)
        }
    }

}
fun addRule(packageName: String, rule: FilterObj, sharedPreferences: SharedPreferences?) {
    packageFilter?.let { packageFilter ->
        sharedPreferences?.let { sharedPreferences ->
            packageFilter.values.put(packageName, rule)
            saveRules(sharedPreferences)
        }
    }
}
fun getRulesForApp(packageName: String):FilterObj?{
    packageFilter?.let {
        return it.values[packageName]
    }
    return null
}
data class FilterObj(
        @SerializedName("key") var key: String,
        @SerializedName("op") var comparisonOp: Int,
        @SerializedName("values") var values: ArrayMap<String, FilterObj>,
        @SerializedName("valuesInt") var valuesInt: ArrayMap<Int, FilterObj>,
        @SerializedName("done") var done: Boolean,
        @SerializedName("remove") var remove:Boolean
){
    constructor(key: String, comparisonOp: Int, done: Boolean, values: ArrayMap<String, FilterObj>) : this(key, comparisonOp, values, ArrayMap<Int, FilterObj>(), done,false) {
    }
    constructor(key: String, comparisonOp: Int, valuesInt: ArrayMap<Int, FilterObj>, done: Boolean) : this(key, comparisonOp,  ArrayMap<String,FilterObj>(),valuesInt, done,false) {
    }
    constructor(done: Boolean,remove: Boolean):this("", NULL_OP,  ArrayMap<String, FilterObj>(), ArrayMap<Int, FilterObj>(), done,remove){}

}
fun saveRules(sharedPreferences: SharedPreferences){
    println(Gson().toJson(packageFilter))
    sharedPreferences.edit().putString("rules",Gson().toJson(packageFilter)).apply()
}
fun loadRules(sharedPreferences: SharedPreferences){
    val json=sharedPreferences.getString("rules","{\"op\":2,\"done\":false,\"key\":\"packageName\",\"remove\":false,\"values\":{},\"valuesInt\":{}}")
    packageFilter=Gson().fromJson(json, FilterObj::class.java)
}
fun isOpIntBased(op: Int): Boolean {
    if (op < INT_OP_EQUALS) return false
    return true
}

fun getValueForKey(key: String, parsedNotification: ParsedNotification): String {
    when (key) {
        "key" -> return parsedNotification.key
        "appName" -> return parsedNotification.appName
        "title" -> return parsedNotification.title
        "summary" -> return parsedNotification.summary
        "subtext" -> return parsedNotification.subtext
        "packageName" -> return parsedNotification.packageName
        "extra" -> return parsedNotification.extra
    }
    return ""
}

fun getValueForKeyInt(key: String, parsedNotification: ParsedNotification): Int {
    when (key) {
        "flags" -> return parsedNotification.flag
        "color" -> return parsedNotification.color
    }
    return 0
}

fun matchAll(filter: FilterObj, parsedNotification: ParsedNotification) = sequence<FilterObj> {
    if (!isOpIntBased(filter.comparisonOp)) {
        val value = getValueForKey(filter.key, parsedNotification)
        when (filter.comparisonOp) {
            STRING_OP_EQUALS -> {
                if (filter.values.contains(value)) {
                    filter.values[value]?.let {
                            yield(it)
                    }
                }
            }
            STRING_OP_CONTAINS -> {
                for (pattern in filter.values) {
                    if (pattern.value != null &&  value.contains(pattern.key)) {
                        yield(pattern.value)
                    }
                }
            }
            STRING_OP_CONTAINS_IGNORECASE -> {
                for (pattern in filter.values) {
                    if (pattern.value != null && (pattern.key=="*" || value.contains(pattern.key,true))) {
                        yield(pattern.value)
                    }
                }
            }
        }
    } else {
        val value = getValueForKeyInt(filter.key, parsedNotification)
        when (filter.comparisonOp) {
            INT_OP_EQUALS -> {
                if (filter.valuesInt.contains(value))
                    filter.valuesInt[value]?.let {
                            yield(it)
                    }
            }
            INT_OP_BITWISEOR -> {
                for (pattern in filter.valuesInt) {
                    if ((value or pattern.key) != 0 && pattern.value != null) {
                        yield(pattern.value)
                    }
                }
            }
            INT_OP_BITWISEAND -> {
                for (pattern in filter.valuesInt) {
                    if ((value and pattern.key) != 0 && pattern.value != null) {
                            yield(pattern.value)
                    }
                }
            }
        }
    }
}
fun query(parsedNotification: ParsedNotification):Boolean{
    return Reputation(parsedNotification.subtext+" "+parsedNotification.summary)
}
fun filter(parsedNotification: ParsedNotification): Boolean {
    //val reputation=GlobalScope.async{query(parsedNotification)}
    packageFilter?.let {
        val nextFilter = recursedFilter(parsedNotification,it)
        nextFilter?.let {
            return it.remove
        }
    }

 //   val result=runBlocking { reputation.await() }
  //  return result
    return false
}

fun recursedFilter(parsedNotification: ParsedNotification, filter: FilterObj): FilterObj? {
    if (filter.done) return filter
    for (nextFilter in matchAll(filter, parsedNotification)) {
        if(nextFilter.done)return nextFilter
        recursedFilter(parsedNotification, nextFilter)?.let {
            return it
        }
    }
    return null
}
