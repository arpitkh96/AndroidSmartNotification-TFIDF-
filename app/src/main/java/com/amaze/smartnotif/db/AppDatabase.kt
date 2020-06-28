package com.ort.gop.Data.db

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.lang.Exception

@Database(entities = arrayOf(ParsedNotification::class,State::class,ExemptedApp::class,Word::class), version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun stateDao(): StateDao
    abstract fun exemptedDao(): ExemptedAppsDao
    abstract fun wordDao(): WordDao

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        fun flush(){
             synchronized(this) {
                if (instance?.isOpen() ?: false) instance?.close();
                instance = null;
            }
        }
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "Messages.db")
                .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                .addCallback(object : Callback() {

                    override fun onCreate(@NonNull db:SupportSQLiteDatabase ) {
                        super.onCreate(db);
                        try {
/*
                            val instance=getInstance(context.applicationContext)
                            instance.unreadDao().addCategory(UnreadCount("REC",0))
                            instance.unreadDao().addCategory(UnreadCount("GROUP",0))
*/
                        }catch (e:Exception){

                        }
                    }
                })
                .build()
        }
    }
}