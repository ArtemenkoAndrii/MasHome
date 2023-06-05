package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.MessageRule

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

    @Insert
    suspend fun insert(messageRule: MessageRule): Long

    @Update
    suspend fun update(messageRule: MessageRule)

    @Delete
    suspend fun delete(messageRule: MessageRule)

    @Upsert
    suspend fun upsert(messageRule: MessageRule): Long
}