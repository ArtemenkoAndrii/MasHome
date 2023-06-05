package com.mas.mobile.domain.message

import com.mas.mobile.domain.budget.SpendingId
import java.time.LocalDateTime

data class Message(
    val id: MessageId,
    var sender: String = "",
    var text: String = "",
    var receivedAt: LocalDateTime = LocalDateTime.now(),
    var ruleId: MessageRuleId = MessageRuleId(-1),
    var spendingId: SpendingId? = null,
    var suggestedExpenditureName: String? = null,
    var suggestedAmount: Double = 0.0,
    var isNew: Boolean = true,
)

@JvmInline
value class MessageId(val value: Int)