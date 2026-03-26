package com.android.mr.todopro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TodoEntity::class],
    version = 3,
    exportSchema = false)
abstract class TodoDatabase: RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        // Singleton - only one instance of the database

        private var DATABASE_NAME = "todo_database"

        @Volatile
        private var INSTANCE: TodoDatabase? = null

        // Add migrations
        val MIGRATION_1_2 = object: Migration(2, 3) {
            override
            fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE todos ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'LOCAL_ONLY'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE todos ADD COLUMN user_id INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        fun getInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    DATABASE_NAME
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
        }
    }
}