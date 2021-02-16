package com.mas.mobile.repository

import com.mas.mobile.repository.db.BaseRepository
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.ExpenditureData
import com.mas.mobile.repository.db.entity.SpendingData
import com.mas.mobile.repository.db.entity.SpendingMessage
import com.mas.mobile.repository.db.entity.SpendingMessageData
import javax.inject.Inject

class SpendingMessageRepository @Inject constructor(
    private val db: AppDatabase
) : BaseRepository<SpendingMessage> {
    val live = Live(db)
    class Live(val db: AppDatabase) {
        fun getAll() = db.spendingMessageDao().getAllLive()
    }

    override fun getById(id: Int) = db.spendingMessageDao().getById(id)

    override fun clone(item: SpendingMessage) = item.copy(data = item.data.copy(id = 0))

    override fun createNew() = SpendingMessage(SpendingMessageData(), SpendingData(), ExpenditureData())

    override suspend fun insert(item: SpendingMessage) = db.spendingMessageDao().insert(item)

    override suspend fun update(item: SpendingMessage) {
        db.spendingMessageDao().insert(item)
    }

    override suspend fun delete(item: SpendingMessage) {
        db.spendingMessageDao().delete(item)
    }
}
