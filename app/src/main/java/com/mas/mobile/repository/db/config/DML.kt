package com.mas.mobile.repository.db.config

import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import com.mas.mobile.repository.db.entity.Qualifier
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

        private fun getCurrency() = Currency.getInstance(Locale.getDefault())

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

        private val language = getLanguage()
        val DEFAULT_QUALIFIERS =
        """
            INSERT INTO qualifiers(name, type) VALUES("payment", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("paid", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("debit", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("debited", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("purchase", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("purchased", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("deduction", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("deducted", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("charged", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("shopping", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("transaction", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("withdrawal", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("transfer", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("invoice", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("bill", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("confirm", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("authorize", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("authorization", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("verify", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("failed", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("decline", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("overdraft", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("cancel", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("overcharge", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("remind", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("whatsapp", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("facebook", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("instagram", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("snapchat", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("twitter", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("tiktok", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("telegram", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("linkedin", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("viber", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("zoom", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("skype", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("slack", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("pinterest", ${Qualifier.SKIP});
        """.plusIf(language == "UK") {
        """
            INSERT INTO qualifiers(name, type) VALUES("покупка", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("оплата", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("рахунок", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("списання", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("підтвердіть", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("підтвердження", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("відміна", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("помилка", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("недостатньо", ${Qualifier.SKIP});
        """ }.plusIf(language == "RU") {
                """
            INSERT INTO qualifiers(name, type) VALUES("покупка", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("оплата", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("счет", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("списание", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("подтвердите", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("подтверждение", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("отмена", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("ошибка", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("недостаточно", ${Qualifier.SKIP});
        """ }.plusIf(language == "ES") {
                """
            INSERT INTO qualifiers(name, type) VALUES("pago", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("pagado", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("cargo", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("compra", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("comprado", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("débito", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("deducción", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("cargado", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("compras", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("transacción", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("retiro", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("transferencia", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("factura", ${Qualifier.CATCH});
            INSERT INTO qualifiers(name, type) VALUES("confirmar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("autorizar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("autorización", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("verificar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("fallido", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("rechazar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("sobregiro", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("cancelar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("sobrecargar", ${Qualifier.SKIP});
            INSERT INTO qualifiers(name, type) VALUES("recordar", ${Qualifier.SKIP});
        """ }.trimIndent()
    }
}

