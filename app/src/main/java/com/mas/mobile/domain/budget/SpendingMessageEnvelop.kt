package com.mas.mobile.domain.budget

class SpendingMessageEnvelop (
    val id: Int,
    val amount: Double,
    val text: String
) {
    override fun toString() = "$id-$amount-$text"

    companion object {
        fun fromString(value: String): SpendingMessageEnvelop? {
            val split = value.split(DELIMITER, limit = 3)
            if(split.size == 3) {
                val id = split[0].toInt()
                val amount = split[1].toDoubleOrNull()!!
                val text = split[2]
                return SpendingMessageEnvelop(id, amount, text)
            }
            return null
        }
        private const val DELIMITER = "-"
    }
}