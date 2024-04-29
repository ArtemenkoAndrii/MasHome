package com.mas.mobile.repository.message

import com.mas.mobile.domain.message.MessageRule
import com.mas.mobile.domain.message.MessageRuleId
import com.mas.mobile.domain.message.Pattern
import com.mas.mobile.toCurrency
import java.util.Currency
import com.mas.mobile.repository.db.entity.MessageRule as MessageRuleData

object MessageRuleMapper {
    fun toModel(dto: MessageRuleData) =
        MessageRule(
            id = MessageRuleId(dto.id),
            name = dto.name,
            pattern = Pattern(dto.amountMatcher),
            expenditureMatcher = dto.expenditureMatcher,
            expenditureName = dto.expenditureName,
            currency = dto.currency.toCurrency()
        )

    fun toDTO(model: MessageRule) =
        MessageRuleData(
            id = model.id.value,
            name = model.name,
            amountMatcher = model.pattern.value,
            expenditureMatcher = model.expenditureMatcher,
            expenditureName = model.expenditureName,
            currency = model.currency.currencyCode,
        )
}