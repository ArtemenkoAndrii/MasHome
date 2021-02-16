package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.MessageRule
import com.mas.mobile.repository.db.entity.MessageRuleData

@Dao
interface MessageRuleDAO {
    @Query("SELECT * FROM message_rules")
    fun getAllLive(): LiveData<List<MessageRule>>

    @Query("SELECT * FROM message_rules")
    fun getAll(): List<MessageRule>

    @Query("SELECT * FROM message_rules WHERE lower(name) = lower(:name)")
    fun getBySender(name: String): List<MessageRule>

    @Query("SELECT * FROM message_rules WHERE id = :id")
    fun getById(id: Int): MessageRule?

    @Ignore
    suspend fun insert(messageRule: MessageRule): Long {
        return insertMessageRuleData(messageRule.data)
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMessageRuleData(messageRuleData: MessageRuleData): Long

    @Ignore
    suspend fun update(messageRule: MessageRule) {
        updateMessageRuleData(messageRule.data)
    }

    @Update
    suspend fun updateMessageRuleData(messageRuleData: MessageRuleData)

    @Ignore
    suspend fun delete(messageRule: MessageRule) {
        deleteMessageRuleData(messageRule.data)
    }

    @Delete
    suspend fun deleteMessageRuleData(messageRuleData: MessageRuleData)
}