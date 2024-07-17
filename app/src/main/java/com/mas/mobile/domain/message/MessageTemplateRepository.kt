package com.mas.mobile.domain.message

import androidx.lifecycle.LiveData
import com.mas.mobile.domain.Repository

interface MessageTemplateRepository : Repository<MessageTemplate> {
    fun getById(id: MessageTemplateId): MessageTemplate?
    fun getAll(): LiveData<List<MessageTemplate>>
    fun create(): MessageTemplate
    fun clone(messageTemplate: MessageTemplate): MessageTemplate
}