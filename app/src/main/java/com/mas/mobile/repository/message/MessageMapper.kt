package com.mas.mobile.repository.message

import com.mas.mobile.domain.budget.SpendingId
import com.mas.mobile.domain.message.Message
import com.mas.mobile.domain.message.MessageId
import com.mas.mobile.domain.message.MessageRuleId
import com.mas.mobile.repository.db.entity.SpendingMessage

object MessageMapper {
    fun toModel(dto: SpendingMessage) =
        Message(
            id = MessageId(dto.id),
            sender = dto.name,
            text = dto.message,
            receivedAt = dto.receivedAt,
            ruleId = MessageRuleId(dto.ruleId),
            spendingId = dto.spendingId?.let { SpendingId(it) },
            suggestedExpenditureName = dto.suggestedExpenditureName,
            suggestedAmount = dto.suggestedAmount,
            isNew = dto.isNew
        )

    fun toDTO(model: Message) =
        SpendingMessage(
            id = model.id.value,
            name = model.sender,
            message = model.text,
            receivedAt = model.receivedAt,
            ruleId = model.ruleId.value,
            spendingId = model.spendingId?.value,
            suggestedExpenditureName = model.suggestedExpenditureName,
            suggestedAmount = model.suggestedAmount,
            isNew = model.isNew
        )
}