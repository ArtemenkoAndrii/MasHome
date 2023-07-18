package com.mas.mobile.domain.message

import android.util.Log
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.service.CoroutineService
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageService @Inject constructor(
    private val coroutineService: CoroutineService,
    private val ruleRepository: MessageRuleRepository,
    private val budgetService: BudgetService,
    val messageRepository: MessageRepository
) {
    fun handleMessage(sender: String, text: String, date: LocalDateTime) {
        val validSender = sender.trim()
        val validText = text.trim()
        when (val message = classify(validSender, validText)) {
            is Captured -> {
                messageRepository.create().also {
                    it.sender = sender
                    it.text = text
                    it.receivedAt = date
                    it.ruleId = message.ruleId
                    it.suggestedAmount = message.amount
                    it.suggestedExpenditureName = message.expenditureName
                    it.isNew = true
                    it.status = Message.Status.MATCHED
                }.also {
                    save(it)
                }
            }
            is Recommended -> {
                messageRepository.create().also {
                    it.sender = sender
                    it.text = text
                    it.receivedAt = date
                    it.suggestedAmount = 0.0
                    it.isNew = true
                    it.status = Message.Status.RECOMMENDED
                }.also {
                    save(it)
                }
            }
            is Rejected -> Log.i(this::class.simpleName, "A message from $validSender doesn't match any rule.")
        }
    }

    fun classify(sender: String, text: String, rules: List<MessageRule> = ruleRepository.getAll()): MessageClass {
        val matchedByAmount = rules
            .filter { sender.contains(it.name) }
            .filter { matchAmount(it.amountMatcher, text) }

        val matchedByExpenditure = matchedByAmount.filter { matchExpenditure(it.expenditureMatcher, text) }

        return when {
            matchedByExpenditure.isNotEmpty() -> {
                val rule = matchedByExpenditure.first()
                Captured(
                    ruleId = rule.id,
                    amount = extractAnyAmount(rule.amountMatcher, text),
                    expenditureName = rule.expenditureName
                )
            }
            matchedByAmount.isNotEmpty() -> {
                val rule = matchedByAmount.first()
                Captured(
                    ruleId = rule.id,
                    amount = extractAnyAmount(rule.amountMatcher, text),
                    expenditureName = null
                )
            }
            isRecommended(text) -> Recommended
            else -> Rejected
        }
    }

    private fun save(message: Message) {
        coroutineService.backgroundTask {
            if (!message.suggestedExpenditureName.isNullOrEmpty() ) {
                message.spendingId = budgetService.spend(
                    date = message.receivedAt,
                    amount = message.suggestedAmount,
                    comment = message.text,
                    expenditureName = message.suggestedExpenditureName ?: ""
                )
            }
            messageRepository.save(message)
        }
    }

    private fun extractAnyAmount(matcher: String, text: String) =
        extractAmount(matcher, text, AMOUNT_WITH_DOT_MASK)
            ?: extractAmount(matcher, text, AMOUNT_WITH_COMMA_MASK)
            ?: 0.0

    private fun extractAmount(matcher: String, text: String, mask: String): Double? =
        matcher.replace(AMOUNT_PLACEHOLDER, mask)
            .toRegex(RegexOption.IGNORE_CASE)
            .find(text)
            ?.let {
                mask.toRegex().find(it.value)?.value?.replace(",",".")?.toDoubleOrNull()
            }

    private fun matchAmount(matcher: String, text: String): Boolean =
        match(matcher.replace(AMOUNT_PLACEHOLDER, AMOUNT_WITH_DOT_MASK), text) ||
                match(matcher.replace(AMOUNT_PLACEHOLDER, AMOUNT_WITH_COMMA_MASK), text)

    private fun matchExpenditure(matcher: String, text: String): Boolean =
        match(matcher, text)

    private fun match(matcher: String, text: String): Boolean =
        if (matcher.isNotBlank()) {
            matcher.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(text)
        } else {
            false
        }

    private fun isRecommended(text: String) =
        CURRENCIES.entries.any {
            text.uppercase().contains(it.key) || text.contains(it.value)
        } && matchAmount(AMOUNT_PLACEHOLDER, text)

    companion object {
        const val AMOUNT_PLACEHOLDER = "{amount}"
        const val AMOUNT_WITH_DOT_MASK = "\\d+.?\\d+"
        const val AMOUNT_WITH_COMMA_MASK = "\\d+,?\\d+"

        val CURRENCIES = mutableMapOf(
            "UAH" to "₴",
            "USD" to "$",
            "EUR" to "€",
            "GBP" to "£",
            "JPY" to "¥",
            "AUD" to "$",
            "CAD" to "$",
            "CHF" to "Fr",
            "CNY" to "¥",
            "RUB" to "₽"
        ).also {
            val instance = Currency.getInstance(Locale.getDefault())
            if (instance != null) {
                it[instance.currencyCode.uppercase()] = instance.symbol
            }
        }
    }

    sealed interface MessageClass

    open class Captured(
        val ruleId: MessageRuleId,
        val amount: Double,
        val expenditureName: String?
    ): MessageClass

    object Recommended : MessageClass

    object Rejected : MessageClass
}
