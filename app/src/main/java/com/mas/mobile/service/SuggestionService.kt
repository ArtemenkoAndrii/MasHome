package com.mas.mobile.service

import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.MessageRuleRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionService @Inject constructor(
    private val messageRuleRepository: MessageRuleRepository,
    private val expenditureRepository: ExpenditureRepository,
    private val budgetService: BudgetService
) {

    fun makeSuggestions(message: Message): Suggestion {
        val rule = messageRuleRepository.getAll()
            .filter { message.sender.contains(it.name) }
            .filter { matchAmount(it.amountMatcher, message.text) }
            .firstOrNull { matchExpenditure(it.expenditureMatcher, message.text) }

        return if (rule != null) {
            val amount = extractAmount(rule.amountMatcher, message.text, AMOUNT_WITH_DOT_MASK)
                ?: extractAmount(rule.amountMatcher, message.text, AMOUNT_WITH_COMMA_MASK)

            AutoSuggestion(rule.id, findExpenditureId(rule.expenditureName), amount ?: 0.0)
        } else {
            NoSuggestion
        }
    }

    private fun findExpenditureId(name: String): Int {
        val expenditureName = name.trim().uppercase()
        val activeBudgetId = budgetService.getActiveOrCreate().id
        val expenditure = expenditureRepository.getByBudgetId(activeBudgetId)
            .firstOrNull { it.name.uppercase() == expenditureName }
        return expenditure?.id ?: createExpenditure(name, activeBudgetId).toInt()
    }

    private fun createExpenditure(name: String, budgetId: Int): Long {
        val newExpenditure = expenditureRepository.createNew().also {
            it.data.name = name
            it.data.budget_id = budgetId
        }
        return runBlocking {
            expenditureRepository.insert(newExpenditure)
        }
    }

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

    companion object {
        const val AMOUNT_PLACEHOLDER = "{amount}"
        const val AMOUNT_WITH_DOT_MASK = "\\d+.?\\d+"
        const val AMOUNT_WITH_COMMA_MASK = "\\d+,?\\d+"
    }
}
