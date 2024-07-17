package com.mas.mobile.repository.db.config

import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import com.mas.mobile.repository.db.entity.Qualifier
import com.mas.mobile.util.CurrencyTools
import java.time.LocalDate
import java.util.Currency
import java.util.Locale

class DML {
    companion object {
        private val maxDate = SQLiteTypeConverter().fromLocalDate(LocalDate.MAX)

        private fun String.plusIf(condition: Boolean, value: () -> String) = this.plus(
            if (condition) {
                value()
            } else {
                ""
            }
        )

        private fun getCurrency() = CurrencyTools.getDefaultCurrency()

        private fun getLanguage() = Locale.getDefault().language.uppercase()

        val TEMPLATE_GENERATOR = """
            INSERT INTO generator(id)
            VALUES(100)
        """.trimIndent()

        val TEMPLATE_BUDGET = """
            INSERT INTO budgets(id, name, startsOn, lastDayAt, plan, fact, isActive, comment, currency)
            VALUES(1, "TEMPLATE", $maxDate, $maxDate, 0.00, 0.00, 0, "Automatically generated", "${getCurrency()}");
        """.trimIndent()

        val TEMPLATE_EXPENDITURES = """
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(1, "Groceries", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(2, "Shopping", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(3, "Restaurants", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(4, "Entertainment", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(5, "Transport", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(6, "Beauty", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(7, "Sports", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(8, "Subscriptions", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(9, "Utilities", 1000.00, 0.00, "Automatically generated", 1);
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(10, "Rent", 1000.00, 0.00, "Automatically generated", 1);
        """.trimIndent()

        val GREETING_MESSAGE_RULES = """
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher, currency)
            VALUES(1, "BANK OF AMERICA", "Food", "walmart", "{amount}", "${getCurrency()}");
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher, currency)
            VALUES(2, "BBVA", "Purchases", "amazon", "Purchase of {amount} EUR", "${getCurrency()}");
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher, currency)
            VALUES(3, "BANK OF SCOT", "Food", "burgerkg", "You spent £{amount} at", "${getCurrency()}");
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher, currency)
            VALUES(4, "Revolut", "Food", "McDonalds", "Paid €{amount} at", "${getCurrency()}");
        """.trimIndent()

        val GREETING_MESSAGE_TEMPLATES = """
            INSERT INTO message_template(id, sender, pattern, example, currency, enabled)
            VALUES(1, "Revolut", "Paid ${'$'}{amount} at {merchant} Spent", "McDonalds 🛍 Paid ${'$'}55.70 at McDonalds Spent today: ${'$'}55.70", "USD", 1);
            INSERT INTO message_template(id, sender, pattern, example, currency, enabled)
            VALUES(2, "BBVA Spain", "of {amount} EUR in {merchant} with", "Payment accepted 👍💳 Payment of 4,10 EUR in aliexpress with your card ending in 1234 accepted.", "EUR", 1);
            INSERT INTO message_template(id, sender, pattern, example, currency, enabled)
            VALUES(3, "Swedbank", "Swedbank {amount} EUR paid at {merchant}", "Notification from Swedbank 7.6 EUR paid at JOHN SMITH", "EUR", 1);
            INSERT INTO message_template(id, sender, pattern, example, currency, enabled)
            VALUES(4, "ING Bankieren", "is {amount} EUR afgeschreven van rekening *007. {merchant}", "ING Bankieren Er is 2,56 EUR afgeschreven van rekening *003. ALBERT HEIJN 1493", "EUR", 1);
        """.trimIndent()

        private val language = getLanguage()
        val DEFAULT_QUALIFIERS =
        """
            INSERT INTO qualifiers(id, name, type) VALUES(1, "payment", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(2, "paid", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(3, "debit", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(4, "debited", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(5, "purchase", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(6, "purchased", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(7, "deduction", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(8, "deducted", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(9, "charged", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(10, "shopping", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(11, "transaction", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(12, "withdrawal", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(13, "transfer", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(14, "invoice", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(15, "bill", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(16, "confirm", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(17, "authorize", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(18, "authorization", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(19, "verify", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(20, "failed", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(21, "decline", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(22, "overdraft", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(23, "cancel", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(24, "overcharge", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(25, "remind", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(26, "whatsapp", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(27, "facebook", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(28, "instagram", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(29, "snapchat", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(30, "twitter", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(31, "tiktok", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(32, "telegram", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(33, "linkedin", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(34, "viber", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(35, "zoom", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(36, "skype", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(37, "slack", ${Qualifier.BLACKLIST});
            INSERT INTO qualifiers(id, name, type) VALUES(38, "pinterest", ${Qualifier.BLACKLIST});
        """.plusIf(language == "UK") {
        """
            INSERT INTO qualifiers(id, name, type) VALUES(39, "покупка", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(40, "оплата", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(41, "рахунок", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(42, "списання", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(43, "підтвердіть", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(44, "підтвердження", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(45, "відміна", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(46, "помилка", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(47, "недостатньо", ${Qualifier.SKIP});
        """ }.plusIf(language == "RU") {
                """
            INSERT INTO qualifiers(id, name, type) VALUES(48, "покупка", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(49, "оплата", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(50, "счет", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(51, "списание", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(52, "подтвердите", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(53, "подтверждение", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(54, "отмена", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(55, "ошибка", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(56, "недостаточно", ${Qualifier.SKIP});
        """ }.plusIf(language == "ES") {
                """
            INSERT INTO qualifiers(id, name, type) VALUES(57, "pago", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(58, "pagado", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(59, "cargo", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(60, "compra", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(61, "comprado", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(62, "débito", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(63, "deducción", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(64, "cargado", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(65, "compras", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(66, "transacción", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(67, "retiro", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(68, "transferencia", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(69, "factura", ${Qualifier.CATCH});
            INSERT INTO qualifiers(id, name, type) VALUES(70, "confirmar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(71, "autorizar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(72, "autorización", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(73, "verificar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(74, "fallido", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(75, "rechazar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(76, "sobregiro", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(77, "cancelar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(78, "sobrecargar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(id, name, type) VALUES(79, "recordar", ${Qualifier.SKIP});
        """ }.trimIndent()
    }
}

