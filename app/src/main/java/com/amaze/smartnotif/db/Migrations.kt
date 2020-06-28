package com.ort.gop.Data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1,2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `ParsedNotification` ADD COLUMN `subtext` TEXT NOT NULL DEFAULT ''")
    }
}
val MIGRATION_2_3 = object : Migration(2,3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `ExemptedApp`  (`packageName` TEXT NOT NULL ,PRIMARY KEY(`packageName`))")
    }
}
val MIGRATION_3_4 = object : Migration(3,4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `Filter`  (`id`  INTEGER NOT NULL DEFAULT 0  PRIMARY KEY AUTOINCREMENT ,`column` TEXT NOT NULL,`op`  INTEGER NOT NULL DEFAULT 0,`stringval` TEXT NOT NULL,`intval`  INTEGER NOT NULL DEFAULT 0)")
    }
}
val MIGRATION_4_5 = object : Migration(4,5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `ParsedNotification` ADD COLUMN `flags` INTEGER NOT NULL DEFAULT 0")
    }
}
val MIGRATION_5_6 = object : Migration(5,6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `Word`  (`id`  INTEGER NOT NULL DEFAULT 0  PRIMARY KEY AUTOINCREMENT ,`word` TEXT NOT NULL,`df`  INTEGER NOT NULL DEFAULT 0, UNIQUE(`word`))")
    }
}