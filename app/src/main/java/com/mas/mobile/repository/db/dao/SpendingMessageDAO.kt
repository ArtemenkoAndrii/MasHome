package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.SpendingMessage
import com.mas.mobile.repository.db.entity.SpendingMessageData

@Dao
interface SpendingMessageDAO {
    @Query("SELECT * FROM spending_messages ORDER BY receivedAt DESC")
    fun getAllLive(): LiveData<List<SpendingMessage>>

    @Query("SELECT * FROM spending_messages WHERE id = :id")
    fun getById(id: Int): SpendingMessage?

    @Ignore
    suspend fun insert(spendingMessage: SpendingMessage): Long =
        insertMessageData(spendingMessage.data)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMessageData(spendingMessage: SpendingMessageData): Long

    @Ignore
    suspend fun update(spendingMessage: SpendingMessage) {
        updateMessageData(spendingMessage.data)
    }

    @Update
    suspend fun updateMessageData(spendingMessage: SpendingMessageData)

    @Ignore
    suspend fun delete(messages: SpendingMessage) {
        deleteMessageData(messages.data)
    }

    @Delete
    suspend fun deleteMessageData(spendingMessage: SpendingMessageData)
}