package com.mas.mobile.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mas.mobile.db.dao.CategoryDAO
import com.mas.mobile.db.entity.CategoryEntity

@Database(entities = [CategoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context,
                    AppDatabase::class.java, "mas_home.db").allowMainThreadQueries().build()
            }
            return INSTANCE!!
        }
    }

    abstract fun categoryDao(): CategoryDAO
}