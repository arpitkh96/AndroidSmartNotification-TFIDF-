package com.amaze.smartnotif.data.tfidf

import android.content.Context
import com.ort.gop.Data.db.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import java.util.concurrent.atomic.AtomicBoolean


public class Tfidfworker(context: Context, coroutineScope: CoroutineScope=GlobalScope){
    var compareJobs= Channel<Pair<String,Channel<Boolean>>>()
    var queuedJobs= Channel<String>()
    var todoJobs= Channel<String>()
    var trainingActive=AtomicBoolean(false)
    var trainingInitialWait=AtomicBoolean(false)
    var vectoriser:Vectoriser
    init {
        vectoriser=Vectoriser(AppDatabase.getInstance(context))
        coroutineScope.launch {
            worker()
        }
    }
    suspend fun worker()= coroutineScope{
        while (isActive) {
            if (!todoJobs.isEmpty && !trainingActive.get() && !trainingInitialWait.get()){
                //Starting job
                launch {
                    triggerRetrain()
                }

            }
            select<Unit> {
                compareJobs.onReceiveOrNull{
                    it?.let {
                        if (!it.second.isClosedForSend){
                            it.second.offer(vectoriser.isBad(it.first))
                        }
                    }
                }
                queuedJobs.onReceiveOrNull{
                   it?.let {
                       todoJobs.send(it)
                       if (!trainingActive.get() && !trainingInitialWait.get()){
                           //Starting job
                           launch {
                               triggerRetrain()
                           }

                       }
                   }
                }
            }
        }
    }
    suspend fun triggerRetrain()= coroutineScope{
        var start=false
        var samples=ArrayList<String>()
        while(!start){
            var ticker= ticker(3000)
            select<Unit> {
                ticker.onReceiveOrNull{
                    start=true
                }
                todoJobs.onReceiveOrNull{
                    it?.let {
                        samples.add(it)
                    }
                    //DO nothing , wait for another 3 seconds
                }

            }
        }
        trainingActive.set(true)
        trainingInitialWait.set(false)
        //Start training
        vectoriser.addSamples(samples)

    }
    fun addTrainingSample(sample:String){
        if (!queuedJobs.isClosedForSend)
            queuedJobs.offer(sample)
    }
    suspend fun compare(sample:String,timeout:Long):Pair<Boolean,Boolean>{ //bad? + success?
        var channel=Channel<Boolean>(1)
        if (!compareJobs.isClosedForSend) {
            compareJobs.offer(Pair(sample,channel))
        }
        var suc=false
        var bad=false
        withTimeoutOrNull(timeout){
            select<Unit> {
                channel.onReceiveOrNull{
                    channel.cancel()
                    it?.let {
                        suc=true
                        bad=it
                    }?:run{
                        suc=false
                    }

                }
            }
        }
        return Pair(bad,suc)
    }
}