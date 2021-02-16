package com.mas.mobile.repository.db.dao

import androidx.room.*
import com.mas.mobile.repository.db.entity.Settings

@Dao
interface SettingsDAO {
    @Query("SELECT * FROM settings WHERE `key` = :key")
    fun getByKey(key: String): Settings?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(settings: Settings): Long

    @Update
    suspend fun update(settings: Settings)

    @Delete
    suspend fun delete(settings: Settings)
}