package com.mas.mobile.service

import com.mas.mobile.repository.MessageRuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionService @Inject constructor(
    private val messageRuleRepository: MessageRuleRepository
) {

    fun makeSuggestions(message: Message): Suggestion {
        val matchesBySenderAndAmount = messageRuleRepository.getBySender(message.sender).filter {
            matchAmount(it.amountMatcher, message.text)
        }

        if (matchesBySenderAndAmount.isEmpty()) {
            return NoSuggestion
        }

        val matchesByExpenditure = matchesBySenderAndAmount.filter {
            matchExpenditure(it.expenditureMatcher, message.text)
        }

        if (matchesByExpenditure.isNotEmpty()) {
            val rule = matchesByExpenditure[0]
            val amount = extractAmount(rule.amountMatcher, message.text)
            return AutoSuggestion(rule.id, rule.expenditureId, amount ?: 0.0)
        }

        val rule = matchesBySenderAndAmount[0]
        val amount = extractAmount(rule.amountMatcher, message.text)
        return ManualSuggestion(rule.id,amount ?: 0.0)
    }

    private fun extractAmount(matcher: String, text: String): Double? =
        matcher.replace(AMOUNT_PLACEHOLDER, AMOUNT_MASK)
            .toRegex(RegexOption.IGNORE_CASE)
            .find(text)
            ?.let {
                AMOUNT_MASK.toRegex().find(it.value)?.value?.toDoubleOrNull()
            }

    private fun matchAmount(matcher: String, text: String): Boolean =
        match(matcher.replace(AMOUNT_PLACEHOLDER, AMOUNT_MASK), text)

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
        const val AMOUNT_MASK = "\\d+.?\\d+"
    }
}
