package com.mas.mobile.domain.message

import com.mas.mobile.domain.budget.SpendingId
import java.time.LocalDateTime

data class Message(
    val id: MessageId,
    var sender: String = "",
    var text: String = "",
    var receivedAt: LocalDateTime = LocalDateTime.now(),
    var ruleId: MessageRuleId? = null,
    var spendingId: SpendingId? = null,
    var suggestedExpenditureName: String? = null,
    var suggestedAmount: Double = 0.0,
    var isNew: Boolean = true,
    var status: Status = Status.MATCHED
) {
    enum class Status {
        MATCHED, RECOMMENDED
    }
}

@JvmInline
value class MessageId(val value: Int)