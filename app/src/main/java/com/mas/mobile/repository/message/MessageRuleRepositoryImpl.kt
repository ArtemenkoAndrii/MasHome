package com.mas.mobile.repository.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mas.mobile.domain.message.MessageRule
import com.mas.mobile.domain.message.MessageRuleId
import com.mas.mobile.domain.message.MessageRuleRepository
import com.mas.mobile.domain.message.Pattern
import com.mas.mobile.repository.db.entity.MessageRule as MessageRuleData
import com.mas.mobile.repository.db.config.AppDatabase
import java.util.Currency
import javax.inject.Singleton

@Singleton
class MessageRuleRepositoryImpl(val db: AppDatabase) : MessageRuleRepository {
    override fun getAll(): List<MessageRule> =
        db.messageRuleDao().getAll().map { it.toModel() }.toList()

    override fun getById(id: MessageRuleId): MessageRule? =
        db.messageRuleDao().getById(id.value)?.toModel()

    override fun getLiveMessageRules(): LiveData<List<MessageRule>> =
        Transformations.map(db.messageRuleDao().getAllLive()) { list ->
            list.map { it.toModel() }.toList()
        }

    override fun create(): MessageRule =
        MessageRule(
            id = MessageRuleId(db.idGeneratorDAO().generateId().toInt()),
            name= "",
            pattern = Pattern(),
            expenditureMatcher = "",
            expenditureName = "",
            currency = Currency.getInstance("EUR")
        )

    override suspend fun save(item: MessageRule) {
        db.messageRuleDao().upsert(item.toDto())
    }

    override suspend fun remove(item: MessageRule) {
        db.messageRuleDao().delete(item.toDto())
    }

    private fun MessageRule.toDto() = MessageRuleMapper.toDTO(this)
    private fun MessageRuleData.toModel() = MessageRuleMapper.toModel(this)
}