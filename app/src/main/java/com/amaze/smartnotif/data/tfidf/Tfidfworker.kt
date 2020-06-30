package com.amaze.smartnotif.data.tfidf

import android.content.Context
import com.ort.gop.Data.db.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random


public class Tfidfworker(context: Context, coroutineScope: CoroutineScope=GlobalScope){
    var compareJobs= Channel<Pair<String,Channel<Boolean>>>(Channel.UNLIMITED)
    var queuedJobs= Channel<String>(Channel.UNLIMITED)
    var todoJobs= Channel<String>(Channel.UNLIMITED)
    var trainingActive=AtomicBoolean(false)
    var trainingInitialWait=AtomicBoolean(false)
    lateinit var vectoriser:Vectoriser
    init {
        coroutineScope.launch {
            vectoriser=Vectoriser(AppDatabase.getInstance(context))
            worker()
        }

    }
    companion object {
        var workerInstance:Tfidfworker?=null
        fun getInstance(context: Context, coroutineScope: CoroutineScope=GlobalScope):Tfidfworker{
            if (workerInstance==null){
                workerInstance= Tfidfworker(context,coroutineScope)
            }
            return workerInstance!!
        }
    }
    suspend fun worker()= coroutineScope{
        println("Worker started")
        while (isActive) {
            println("${todoJobs.isEmpty} ${ !trainingActive.get()} && ${ !trainingInitialWait.get()}")
            if (!todoJobs.isEmpty && !trainingActive.get() && !trainingInitialWait.get()){
                //Starting job
                launch {
                    delay(1000)
                    if (!todoJobs.isEmpty && !trainingActive.get() && !trainingInitialWait.get())
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
                       println("Got sample $it ${trainingActive.get()} ${trainingInitialWait.get()} ${todoJobs.offer(it)}")
                       if (!trainingActive.get() && !trainingInitialWait.get()){
                           //Starting job
                           println("Starting training")

                           launch(CoroutineName("X${Random(4444).nextInt()}")){
                               triggerRetrain()
                           }

                       }
                   }
                }
            }
        }
        println("Worker died")
    }
    suspend fun triggerRetrain()= coroutineScope{
        println("Retrain called")
        trainingInitialWait.set(true)
        var start=false
        var samples=ArrayList<String>()
        var ticker= ticker(3000)
        while(!start){
            println("Waiting for more samples ${Thread.currentThread().name}")
            select<Unit> {
                ticker.onReceiveOrNull{
                    println("Ticker ticked ${Thread.currentThread().name}")
                    start=true
                    ticker.cancel()
                }
                todoJobs.onReceiveOrNull{
                    println("Got one sample ${Thread.currentThread().name}")
                    ticker=ticker(3000)
                    it?.let {
                        samples.add(it)
                    }
                    //DO nothing , wait for another 3 seconds
                }

            }
        }
        println("Number of samples ${samples.size} ${Thread.currentThread().name}")
        trainingActive.set(true)
        trainingInitialWait.set(false)
        //Start training
        println("Training started")
        vectoriser.addSamples(samples)

    }
    fun addTrainingSample(sample:String){
        if (!queuedJobs.isClosedForSend)
        {
            println("Adding sample $sample")
            queuedJobs.offer(sample)
        }
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