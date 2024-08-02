package com.mas.mobile.domain.message

import android.util.Log
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.domain.budget.CategoryService
import com.mas.mobile.domain.settings.SettingsService
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
    private val settings: SettingsService,
    private val analytics: Analytics,
    val repository: MessageRepository
) {
    fun handleRawMessage(sender: String, text: String, date: LocalDateTime) = coroutineService.backgroundTask {
        val status = assessStatus(sender, text)
        analytics.logEvent(Analytics.Event.MESSAGE_EVALUATED, Analytics.Param.STATUS, status::class.simpleName ?: "")

        if (status !is Message.Rejected) {
            val message = repository.create().also {
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

            repository.save(message)
        } else {
            Log.i(this::class.simpleName, "A message from $sender doesn't match any rule.")
        }
    }

    fun promoteRecommendedMessage(message: Message, template: MessageTemplate): Message? {
        val newStatus = matchesTemplate(message.sender, message.text, template)
        return if (newStatus is Message.Matched) {
            message.copy(status = newStatus)
        } else {
            null
        }
    }

    private fun matchesTemplate(sender: String, text: String, template: MessageTemplate): Message.Status? {
        if (!sender.equals(template.sender, true)) {
            return null
        }

        val result = template.parse(text) ?: return null

        return if (template.isEnabled) {
            Message.Matched(
                messageTemplateId = template.id,
                amount = result.amount,
                merchant = result.merchant
            )
        } else {
            Message.Rejected
        }
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

    private fun assessStatus(sender: String, text: String): Message.Status =
        isMatched(sender, text) ?: isRecommended(sender, text) ?: Message.Rejected

    private fun isMatched(sender: String, text: String): Message.Status? =
        messageTemplateRepository.getAll()
            .asSequence()
            .map { matchesTemplate(sender, text, it) }
            .filterNotNull()
            .firstOrNull()

    private fun isRecommended(sender: String, text: String): Message.Status? =
        if (qualifierService.isRecommended(sender, text) && settings.autodetect) {
            Message.Recommended
        } else {
            null
        }
}
