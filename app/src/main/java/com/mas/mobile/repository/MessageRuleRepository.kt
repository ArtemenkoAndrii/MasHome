package com.mas.mobile.repository

import com.mas.mobile.repository.db.BaseRepository
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.MessageRule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRuleRepository @Inject constructor(
    private val db: AppDatabase
) : BaseRepository<MessageRule> {
    val live = Live(db)
    class Live(val db: AppDatabase) {
        fun getAll() = db.messageRuleDao().getAllLive()
    }

    override fun getById(id: Int) = db.messageRuleDao().getById(id)

    override fun clone(item: MessageRule) = item.copy(id = 0)

    override fun createNew() = MessageRule()

    fun getAll() = db.messageRuleDao().getAll()

    override suspend fun insert(item: MessageRule): Long = db.messageRuleDao().insert(item)

    override suspend fun update(item: MessageRule) {
        db.messageRuleDao().update(item)
    }

    override suspend fun delete(item: MessageRule) {
        db.messageRuleDao().delete(item)
    }
}