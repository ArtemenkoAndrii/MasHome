package com.mas.mobile.presentation.viewmodel

import com.mas.mobile.domain.message.Message
import com.mas.mobile.domain.message.MessageRepository
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDate

class MessageListViewModel @AssistedInject constructor(
    private val messageRepository: MessageRepository,
    private val coroutineService: CoroutineService,
    @Assisted param: String
): ListViewModel<Message>(coroutineService, messageRepository) {
    val messages = messageRepository.getLiveMessages(
        LocalDate.now().minusDays(30),
        Message.Status.MATCHED
    )

    fun markAsRead(item: Message) {
        coroutineService.backgroundTask {
            item.isNew = false
            messageRepository.save(item)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(param: String): MessageListViewModel
    }
}
