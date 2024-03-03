package com.mas.mobile.domain.message

import android.util.Log
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.domain.budget.SpendingId
import com.mas.mobile.service.CoroutineService
import com.mas.mobile.service.TaskService
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageService @Inject constructor(
    private val coroutineService: TaskService,
    private val ruleRepository: MessageRuleRepository,
    private val budgetService: BudgetService,
    private val qualifierService: QualifierService,
    val messageRepository: MessageRepository
) {
    fun handleMessage(sender: String, text: String, date: LocalDateTime) {
        val status = evaluateStatus(sender.trim(), text.trim())
        if (status !is Message.Rejected) {
            val message = messageRepository.create().also {
                it.sender = sender
                it.text = text
                it.receivedAt = date
                it.status = status
                it.isNew = true
            }
            save(message)
        } else {
            Log.i(this::class.simpleName, "A message from $sender doesn't match any rule.")
        }
    }

    private fun evaluateStatus(sender: String, text: String, rules: List<MessageRule> = ruleRepository.getAll()): Message.Status {
        data class R(val rule: MessageRule, val match: MessageRule.Match)

        val result = rules
            .mapNotNull { rule ->
                rule.evaluate(sender, text).let {
                    if (it is MessageRule.Match) { R(rule, it) } else null
                }
            }
            .let { r ->
                r.firstOrNull { it.match.expenditureName != null } ?: r.firstOrNull()
            }

        return when {
            result != null -> {
                Message.Matched(
                    ruleId = result.rule.id,
                    suggestedAmount = result.match.amount,
                    suggestedExpenditureName = result.match.expenditureName
                )
            }
            qualifierService.isRecommended(text) -> Message.Recommended
            else -> Message.Rejected
        }
    }

    private fun save(message: Message) = coroutineService.backgroundTask {
        (message.status as? Message.Matched)?.let {
            if (it.suggestedExpenditureName != null) {
                message.spendingId = budgetService.spend(
                    date = message.receivedAt,
                    amount = it.suggestedAmount,
                    expenditureName = it.suggestedExpenditureName,
                    comment = message.text
                )
            }
        }
        messageRepository.save(message)
    }
}
