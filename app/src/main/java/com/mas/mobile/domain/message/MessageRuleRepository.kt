package com.mas.mobile.domain.message

import androidx.lifecycle.LiveData
import com.mas.mobile.domain.Repository

interface MessageRuleRepository : Repository<MessageRule> {
    fun getAll(): List<MessageRule>
    fun getById(id: MessageRuleId): MessageRule?

    fun getLiveMessageRules(): LiveData<List<MessageRule>>

    fun create(): MessageRule
}