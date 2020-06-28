package com.amaze.smartnotif.utils

import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import java.util.*

fun GetUTCTime(): Long {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis/1000
}
data class Action(
        val id:String,
        val title: String,
        val description: String,
        var lastStartTime:Long,
        var lastEndTime:Long,
        var periodSec:Long,
        var duration:Long,
        val state:Int,
        var pending:Boolean,
        val clickActions:Array<String>){
    public fun isPending():Boolean{
        return pending && Date(GetUTCTime()*1000).after(Date((lastStartTime+periodSec)*1000))
    }
}
class AlertHandler(val pushNotification:(title:String,description:String,action:Array<String>)->Unit){
    val lastRestNotificationTime= GetUTCTime()
    val actions= mapOf<String,Action>(
            //"rest" to Action("rest","Take rest","Close eyes for 1 minute",lastRestNotificationTime,60*20,1*60,false, arrayOf("Start"))
    )
    suspend fun start()= coroutineScope{
        val channel=ticker(60*1000)
        while (isActive){
            select<Unit> {
                channel.onReceiveOrNull{
                    for(action in actions.values){
                        if (action.isPending()){
                             ExecuteAction(action)
                        }
                    }
                }
            }
        }

    }
    fun ExecuteAction(action: Action){
        when(action.id){
            "rest"->{
                pushNotification(action.title,action.description,action.clickActions)
        //        action.isActive=true
            }
        }
    }
    fun ActionCallback(id:String){

    }
}