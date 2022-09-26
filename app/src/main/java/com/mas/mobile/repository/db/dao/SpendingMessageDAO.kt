package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.SpendingMessage

@Dao
interface SpendingMessageDAO {
    @Query("SELECT * FROM spending_messages ORDER BY receivedAt DESC")
    fun getAllLive(): LiveData<List<SpendingMessage>>

    @Query("SELECT * FROM spending_messages WHERE id = :id")
    fun getById(id: Int): SpendingMessage?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(spendingMessage: SpendingMessage): Long

    @Update
    suspend fun update(spendingMessage: SpendingMessage)

    @Delete
    suspend fun delete(spendingMessage: SpendingMessage)
}