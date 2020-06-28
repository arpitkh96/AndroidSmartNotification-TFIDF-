package com.ort.gop.Data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import javax.sql.DataSource

@Dao
interface NotificationDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category:ParsedNotification)

}
@Dao
interface StateDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category:State)
}
@Dao
interface ExemptedAppsDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(app:ExemptedApp)
    @Query("SELECT * FROM ExemptedApp order by packageName")
    fun getAll():LiveData<List<ExemptedApp>>
}

@Dao
interface WordDao{
    @Query("SELECT count(*) from word")
    fun getTotalWords():Int

    @Query("SELECT df from word where word=:wordx")
    fun getDf(wordx:String):Int

    @Query("UPDATE word set df=:count where word=:word")
    fun updateWordCount(word: String,count: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(word:Word):Long

    @Delete
    fun deleteWord(word: Word)

    @Query("SELECT * FROM word where word=:word")
    fun getWord(word: String):Word

    @Query("SELECT * from word order by id;")
    fun getAllWords():Array<Word>

}
