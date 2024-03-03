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
            spendingId = dto.spendingId?.let { SpendingId(it) },
            status = if (dto.status == SpendingMessage.MATCHED) {
                        Message.Matched(
                            ruleId = MessageRuleId(dto.ruleId),
                            suggestedAmount = dto.suggestedAmount,
                            suggestedExpenditureName = dto.suggestedExpenditureName
                        )
                     } else {
                        Message.Recommended
                     },
            isNew = dto.isNew
        )

    fun toDTO(model: Message) =
        with (model.status as? Message.Matched) {
            SpendingMessage(
                id = model.id.value,
                name = model.sender,
                message = model.text,
                receivedAt = model.receivedAt,
                status = if (this != null) SpendingMessage.MATCHED else SpendingMessage.RECOMMENDED,
                ruleId = this?.ruleId?.value ?: -1,
                suggestedExpenditureName = this?.suggestedExpenditureName,
                suggestedAmount = this?.suggestedAmount ?: 0.0,
                spendingId = model.spendingId?.value,
                isNew = model.isNew,
            )
        }
}