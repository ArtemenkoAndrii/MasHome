package com.mas.mobile.presentation.viewmodel

import com.mas.mobile.repository.SpendingMessageRepository
import com.mas.mobile.repository.db.entity.SpendingMessage
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MessageListViewModel @AssistedInject constructor(
    private val messageRepository: SpendingMessageRepository,
    private val coroutineService: CoroutineService,
    @Assisted param: String
): BaseListViewModel<SpendingMessage>(coroutineService) {
    val messages = messageRepository.live.getAll()

    override fun getRepository() = messageRepository

    fun markAsRead(item: SpendingMessage) {
        coroutineService.backgroundTask {
            item.isNew = false
            messageRepository.update(item)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(param: String): MessageListViewModel
    }
}
