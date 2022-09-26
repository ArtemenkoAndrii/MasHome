package com.mas.mobile.service

import com.mas.mobile.repository.MessageRuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionService @Inject constructor(
    private val messageRuleRepository: MessageRuleRepository
) {

    fun makeSuggestions(message: Message): Suggestion {
        val bySenderAndAmount = messageRuleRepository.getAll()
            .filter { message.sender.contains(it.name) }
            .filter { matchAmount(it.amountMatcher, message.text) }

        if (bySenderAndAmount.isEmpty()) {
            return NoSuggestion
        }

        val alsoByExpenditure =  bySenderAndAmount
            .firstOrNull { matchExpenditure(it.expenditureMatcher, message.text) }

        return if (alsoByExpenditure != null) {
            val amount = extractAnyAmount(alsoByExpenditure.amountMatcher, message.text)
            AutoSuggestion(
                alsoByExpenditure.id,
                alsoByExpenditure.expenditureName,
                amount ?: 0.0,
                message.date
            )
        } else {
            val manual = bySenderAndAmount.first()
            val amount = extractAnyAmount(manual.amountMatcher, message.text)
            ManualSuggestion(manual.id, amount ?: 0.0, message.date)
        }
    }

    private fun extractAnyAmount(matcher: String, text: String) =
        extractAmount(matcher, text, AMOUNT_WITH_DOT_MASK)
            ?: extractAmount(matcher, text, AMOUNT_WITH_COMMA_MASK)

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
