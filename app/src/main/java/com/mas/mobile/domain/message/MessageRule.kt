package com.mas.mobile.domain.message

data class MessageRule(
    var id: MessageRuleId,
    var name: String = "",
    var amountMatcher: String = "{amount}",
    var expenditureMatcher: String = "",
    var expenditureName: String = "",
)

@JvmInline
value class MessageRuleId(val value: Int)