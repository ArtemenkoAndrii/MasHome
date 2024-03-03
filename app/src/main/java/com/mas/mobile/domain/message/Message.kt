package com.mas.mobile.domain.message

import com.mas.mobile.domain.budget.SpendingId
import java.time.LocalDateTime

data class Message(
    val id: MessageId,
    var sender: String,
    var text: String,
    var receivedAt: LocalDateTime = LocalDateTime.now(),
    var spendingId: SpendingId? = null,
    var status: Status,
    var isNew: Boolean = true
) {
    fun hasSpending() = spendingId != null

    fun toMatched(ruleId: MessageRuleId, suggestedAmount: Double, suggestedExpenditureName: String?) =
        this.copy(
            status = Matched(ruleId, suggestedAmount, suggestedExpenditureName)
        )

    sealed class Status
    object Recommended: Status()
    object Rejected: Status()
    data class Matched(
        val ruleId: MessageRuleId,
        val suggestedAmount: Double,
        val suggestedExpenditureName: String?
    ): Status()
}

@JvmInline
value class MessageId(val value: Int)