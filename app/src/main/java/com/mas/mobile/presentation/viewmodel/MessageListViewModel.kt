package com.mas.mobile.presentation.viewmodel

import com.mas.mobile.repository.SpendingMessageRepository
import com.mas.mobile.repository.db.entity.SpendingMessage
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MessageListViewModel @AssistedInject constructor(
    private val messageRepository: SpendingMessageRepository,
    coroutineService: CoroutineService,
    @Assisted param: String
): BaseListViewModel<SpendingMessage>(coroutineService) {
    val messages = messageRepository.live.getAll()

    override fun getRepository() = messageRepository

    @AssistedFactory
    interface Factory {
        fun create(param: String): MessageListViewModel
    }
}
