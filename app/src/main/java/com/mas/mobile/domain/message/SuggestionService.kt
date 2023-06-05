package com.mas.mobile.domain.message

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionService @Inject constructor(
    private val ruleRepository: MessageRuleRepository
) {

    fun makeSuggestions(sender: String, text: String): Suggestion {
        val rulesForSender = ruleRepository.getAll()
            .filter { sender.contains(it.name) }
            .filter { matchAmount(it.amountMatcher, text) }

        if (rulesForSender.isEmpty()) {
            return NoSuggestion
        }

        val rulesWithExpenditure =
            rulesForSender.firstOrNull { matchExpenditure(it.expenditureMatcher, text) }

        return if (rulesWithExpenditure != null) {
            val amount = extractAnyAmount(rulesWithExpenditure.amountMatcher, text) ?: 0.0
            AutoSuggestion(
                rulesWithExpenditure.id,
                amount,
                rulesWithExpenditure.expenditureName,
            )
        } else {
            val manual = rulesForSender.first()
            val amount = extractAnyAmount(manual.amountMatcher, text)
            ManualSuggestion(manual.id, amount ?: 0.0)
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
