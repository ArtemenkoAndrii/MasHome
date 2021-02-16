package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.mas.mobile.repository.MessageRuleRepository
import com.mas.mobile.repository.db.entity.MessageRule
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MessageRuleListViewModel @AssistedInject constructor(
    private val rulesRepository: MessageRuleRepository,
    @Assisted param: String
): ViewModel() {
    val messageRules = rulesRepository.live.getAll()

    fun remove(item: MessageRule) {
        GlobalScope.launch {
            rulesRepository.delete(item)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(param: String): MessageRuleListViewModel
    }
}