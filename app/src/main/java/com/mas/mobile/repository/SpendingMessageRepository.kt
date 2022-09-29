package com.mas.mobile.repository

import com.mas.mobile.repository.db.BaseRepository
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.SpendingMessage
import javax.inject.Inject

class SpendingMessageRepository @Inject constructor(
    private val db: AppDatabase
) : BaseRepository<SpendingMessage> {
    val live = Live(db)
    class Live(val db: AppDatabase) {
        fun getAll() = db.spendingMessageDao().getAllLive()
    }

    override fun getById(id: Int) = db.spendingMessageDao().getById(id)

    override fun clone(item: SpendingMessage) = item.copy(id = 0)

    override fun createNew() = SpendingMessage()

    override suspend fun insert(item: SpendingMessage) = db.spendingMessageDao().insert(item)

    fun countUnreadLive() = db.spendingMessageDao().countUnreadLive()

    override suspend fun update(item: SpendingMessage) {
        db.spendingMessageDao().update(item)
    }

    override suspend fun delete(item: SpendingMessage) {
        db.spendingMessageDao().delete(item)
    }
}
