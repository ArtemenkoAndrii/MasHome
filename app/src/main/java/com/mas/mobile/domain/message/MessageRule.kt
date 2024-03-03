package com.mas.mobile.domain.message

data class MessageRule(
    var id: MessageRuleId,
    var name: String,
    var pattern: Pattern,
    var expenditureMatcher: String,
    var expenditureName: String
) {
    fun evaluate(sender:String, message: String): Result {
        if (!sender.contains(name, ignoreCase = true)) {
            return NoMatch
        }

        return when (val value = pattern.parse(message)) {
            is Pattern.Data -> {
                Match(
                    amount = value.amount,
                    merchant = value.merchant,
                    expenditureName = if (message.contains(expenditureMatcher, ignoreCase = true)) {
                        expenditureName
                    } else {
                        null
                    }
                )
            }
            else ->  NoMatch
        }
    }

    sealed class Result
    object NoMatch : Result()
    data class Match(
        val amount: Double,
        val merchant: String?,
        val expenditureName: String?
    ) : Result()
}

@JvmInline
value class MessageRuleId(val value: Int)