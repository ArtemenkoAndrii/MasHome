package com.mas.mobile.domain.message

import android.util.Log
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.domain.budget.CategoryService
import com.mas.mobile.service.TaskService
import com.mas.mobile.util.Analytics
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageService @Inject constructor(
    private val coroutineService: TaskService,
    private val messageTemplateRepository: MessageTemplateRepository,
    private val categoryService: CategoryService,
    private val budgetService: BudgetService,
    private val qualifierService: QualifierService,
    private val analytics: Analytics,
    val messageRepository: MessageRepository
) {
    fun handleMessage(sender: String, text: String, date: LocalDateTime) = coroutineService.backgroundTask {
        val status = evaluateStatus(sender.trim(), text.trim())
        analytics.logEvent(Analytics.Event.MESSAGE_EVALUATED, Analytics.Param.STATUS, status::class.simpleName ?: "")

        if (status !is Message.Rejected) {
            val message = messageRepository.create().also {
                it.sender = sender
                it.text = text
                it.receivedAt = date
                it.status = status
                it.isNew = true
            }

            if (status is Message.Matched) {
                createSpending(message)
                analytics.logEvent(Analytics.Event.SPENDING_CREATED, Analytics.Param.SOURCE, "auto")
            }

            messageRepository.save(message)
        } else {
            Log.i(this::class.simpleName, "A message from $sender doesn't match any rule.")
        }
    }

    fun evaluateMessageStatus(template: MessageTemplate, text: String): Message.Matched? =
        template.parse(text)?.let { result ->
            Message.Matched(
                messageTemplateId = template.id ,
                amount = result.amount,
                merchant = result.merchant
            )
        }

    private suspend fun createSpending(message: Message) {
        val status = message.status as? Message.Matched ?: return
        val merchant = status.merchant ?: return
        val category = categoryService.findCategoryByMerchant(merchant) ?: return
        val currency = messageTemplateRepository.getById(status.messageTemplateId)?.currency ?: return

        message.spendingId = budgetService.spend(
            date = message.receivedAt,
            amount = status.amount,
            expenditureName = category.name,
            comment = message.text,
            currency = currency
        )
    }

    private fun evaluateStatus(sender: String, text: String): Message.Status =
        findMatched(sender, text) ?: findRecommended(sender, text) ?: Message.Rejected

    private fun findMatched(sender: String, text: String): Message.Matched? =
        messageTemplateRepository.getAll()
            .asSequence()
            .filter { it.sender.equals(sender, true) }
            .map { evaluateMessageStatus(it, text) }
            .firstOrNull()

    private fun findRecommended(sender: String, text: String): Message.Recommended? =
        if (qualifierService.isRecommended(sender, text)) {
            Message.Recommended
        } else {
            null
        }
}
