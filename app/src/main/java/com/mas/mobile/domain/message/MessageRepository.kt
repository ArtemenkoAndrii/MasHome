package com.mas.mobile.domain.message

import androidx.lifecycle.LiveData
import com.mas.mobile.domain.Repository
import java.time.LocalDate

interface MessageRepository : Repository<Message> {
    fun getById(id: MessageId): Message?
    fun create(): Message

    fun getLiveMessages(from: LocalDate, status: Message.Status): LiveData<List<Message>>
    fun countUnreadLive(from: LocalDate): LiveData<Int>
}