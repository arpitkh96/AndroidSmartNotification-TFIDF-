package com.amaze.smartnotif.data.tfidf

import android.util.SparseArray
import androidx.core.util.contains
import androidx.core.util.forEach
import com.amaze.smartnotif.utils.RWMutex
import com.ort.gop.Data.db.AppDatabase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.locks.ReadWriteLock
import kotlin.math.ln
import kotlin.math.sqrt

class Vectoriser (var db:AppDatabase){
    var wordStore:WordStore
    val sampleWords=ArrayList<Pair<HashMap<String,Int>,Int>>()
    val sampleVectors=ArrayList<SparseArray<Float>>()
    var rwmutex=RWMutex()
    init {
        wordStore= WordStore(db)
    }

    suspend fun addSamples(sentences:ArrayList<String>){
        val tf=ArrayList<HashMap<String,Int>>()

        for (sentence in sentences){
            var words=tokenise(sentence)
            var wordMap= hashMapOf<String,Int>()
            words.forEach { word->
                wordMap[word]=wordMap[word]?.plus(1)?:1
            }
            tf.add(wordMap)
            sampleWords.add(Pair(wordMap,words.size))
        }

        val df=documentFreq(tf)
        println("Getting lock")
        rwmutex.writeLock()
        println("Got lock")

        //We cant let Df change parallely while we are also using the model
        df.forEach { word, freq ->
            wordStore.addDf(word,freq)
        }
        vectoriseSamples()
        rwmutex.writeUnlock()
        println("Done training")


    }
    suspend fun documentFreq(samples:ArrayList<HashMap<String,Int>>):HashMap<String,Int>{
        val map=HashMap<String,Int>()
        for (sample in samples){
            for (word in sample.keys)
                map[word]=map[word]?.plus(1)?:1
        }
        return map
    }
    suspend fun tfidf(sample: HashMap<String, Int>,lenOfSample:Int,numberOfSamples:Int):SparseArray<Float>{
        val array=SparseArray<Float>(15)
        for ((word,tf) in sample){
            val freq=(tf.toFloat()/lenOfSample)*ln(numberOfSamples.toFloat()/(wordStore.getWord(word)?.df?:1))
            array.put(wordStore.getId(word),freq)
        }
        return array
    }
    suspend fun normalise(array:SparseArray<Float>){
        var sum=0F
        array.forEach { key, value ->
            sum+=value*value
        }
        sum=sqrt(sum)
        array.forEach { key, value ->
            array.setValueAt(key,value/sum)
        }
    }
    suspend fun calculateSimilarity(samples: ArrayList<SparseArray<Float>>,input:SparseArray<Float>):Array<Float>{
        val array= Array<Float>(samples.size,{ 0F })
        var index=0
        for (sample in samples){
            array[index]=similarity(sample,input)
            index++
        }
        return array
    }

    fun similarity(input1:SparseArray<Float>,input2:SparseArray<Float>) :Float{
        var sum=0F
        input1.forEach { key, value ->
            if (input2.contains(key)){
                sum+=input2.get(key)*value
            }
        }
        return sum
    }
    suspend fun vectoriseSamples(){
        sampleVectors.clear()
        for (sample in sampleWords){
            val array=tfidf(sample.first,sample.second,sampleWords.size)
            sampleVectors.add(array)
        }
    }
    suspend fun isBad(sentence:String):Boolean{
        val words=tokenise(sentence)
        var wordMap= hashMapOf<String,Int>()
        words.forEach { word->
            wordMap[word]=wordMap[word]?.plus(1)?:1
        }
        val array=tfidf(wordMap,words.size,sampleWords.size)
        rwmutex.readLock()
        val similarity=calculateSimilarity(sampleVectors,array)
        rwmutex.readUnlock()
        val index=argsort(similarity)
        return similarity[index[0]]>0.5
    }

    fun argsort(array:Array<Float>):Array<Int>{
        val arr=Array<Int>(array.size,{it})
        arr.sortWith(object :Comparator<Int>{
            override fun compare(o1: Int, o2: Int): Int {
                return -1 * array[o1].compareTo(array[o2])
            }
        })
        return arr
    }
}
