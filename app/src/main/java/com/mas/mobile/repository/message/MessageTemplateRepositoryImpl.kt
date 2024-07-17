package com.mas.mobile.repository.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mas.mobile.domain.message.MessageTemplate
import com.mas.mobile.domain.message.MessageTemplateId
import com.mas.mobile.domain.message.MessageTemplateRepository
import com.mas.mobile.domain.message.Pattern
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.util.CurrencyTools
import java.util.Currency
import javax.inject.Singleton
import com.mas.mobile.repository.db.entity.MessageTemplate as MessageTemplateData

@Singleton
class MessageTemplateRepositoryImpl(val db: AppDatabase) : MessageTemplateRepository {
    val dao = db.messageTemplateDAO()
    override fun getById(id: MessageTemplateId): MessageTemplate? =
        dao.getById(id.value)?.toModel()

    override fun getAll(): LiveData<List<MessageTemplate>> =
        Transformations.map(dao.getAllLive()) { list ->
            list.map { it.toModel() }.toList()
        }

    override fun create(): MessageTemplate =
        MessageTemplate(
            id = MessageTemplateId(db.idGeneratorDAO().generateId().toInt()),
            sender = "",
            pattern = Pattern.SIMPLE,
            example = "",
            currency = CurrencyTools.getDefaultCurrency(),
            isEnabled = true
        )

    override fun clone(messageTemplate: MessageTemplate): MessageTemplate =
        messageTemplate.copy(id = MessageTemplateId(db.idGeneratorDAO().generateId().toInt()))

    override suspend fun save(item: MessageTemplate) {
        dao.upsert(item.toDTO())
    }

    override suspend fun remove(item: MessageTemplate) {
        dao.delete(item.toDTO())
    }
}

private fun MessageTemplate.toDTO() = MessageTemplateMapper.toDTO(this)
private fun MessageTemplateData.toModel() = MessageTemplateMapper.toModel(this)

