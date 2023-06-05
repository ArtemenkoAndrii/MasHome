package com.mas.mobile.presentation.viewmodel

import com.mas.mobile.domain.message.MessageRule
import com.mas.mobile.domain.message.MessageRuleRepository
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MessageRuleListViewModel @AssistedInject constructor(
    messageRuleRepository: MessageRuleRepository,
    coroutineService: CoroutineService,
    @Assisted param: String
): ListViewModel<MessageRule>(coroutineService, messageRuleRepository) {
    val messageRules = messageRuleRepository.getLiveMessageRules()

    @AssistedFactory
    interface Factory {
        fun create(param: String): MessageRuleListViewModel
    }
}