package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.mas.mobile.domain.message.MessageTemplateRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MessageTemplateListViewModel @AssistedInject constructor(
    repository: MessageTemplateRepository,
    @Assisted param: String
): ViewModel() {
    val senders = repository.getAll()

    @AssistedFactory
    interface Factory {
        fun create(param: String): MessageTemplateListViewModel
    }
}
