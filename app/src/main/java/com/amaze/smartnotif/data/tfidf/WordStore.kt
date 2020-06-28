package com.amaze.smartnotif.data.tfidf

import com.ort.gop.Data.db.AppDatabase
import com.ort.gop.Data.db.Word

class WordStore(val db:AppDatabase) {
    var countMap:HashMap<String,Word>
    init {
        countMap= hashMapOf<String,Word>()
        db.wordDao().getAllWords().forEach {
            countMap[it.word]=it
        }
    }
    fun addDf(word: String,freq:Int){
        var existing=countMap[word]
        countMap[word]?.let {
            it.df=it.df?.plus(freq)
            setDfForWord(word,freq)
        }?: kotlin.run {
            val wordObj=Word(word,freq)
            wordObj.id=db.wordDao().insert(wordObj)
            countMap[word]=wordObj
        }
    }
    fun setDfForWord(word: String,count:Int){
        if (db.wordDao().insert(Word(word,count))<0)
            db.wordDao().updateWordCount(word,count)
    }

    fun getWord(word: String):Word?{
        return countMap[word]
    }
    fun getId(word: String):Int{
        return countMap[word]?.id?.toInt()?:-1
    }
    fun getTotalWords():Int{
        return countMap.size
    }
}

