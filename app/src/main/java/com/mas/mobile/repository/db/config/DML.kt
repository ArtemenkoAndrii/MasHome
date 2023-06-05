package com.mas.mobile.repository.db.config

import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DML {
    companion object {
        private val nowAsLong = SQLiteTypeConverter().fromLocalDate(LocalDate.now())

        val TEMPLATE_GENERATOR = """
            INSERT INTO generator(id)
            VALUES(100)
        """.trimIndent()

        val TEMPLATE_BUDGET = """
            INSERT INTO budgets(id, name, startsOn, lastDayAt, plan, fact, isActive, comment)
            VALUES(1, "TEMPLATE", $nowAsLong, $nowAsLong, 0.00, 0.00, 0, "Automatically generated");
        """.trimIndent()

        val TEMPLATE_EXPENDITURES1 = """
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(1, "Food", 100.00, 0.00, "Automatically generated", 1);
        """.trimIndent()

        val TEMPLATE_EXPENDITURES2 = """
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(2, "Purchases", 1000.00, 0.00, "Automatically generated", 1);
        """.trimIndent()

        val GREETING_MESSAGE_RULES_1 = """
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher)
            VALUES(1, "BANK OF AMERICA", "Food", "walmart", "{amount}");
        """.trimIndent()

        val GREETING_MESSAGE_RULES_2 = """
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher)
            VALUES(2, "BBVA", "Purchases", "amazon", "Purchase of {amount} EUR");
        """.trimIndent()

        val GREETING_MESSAGE_RULES_3 = """
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher)
            VALUES(3, "BANK OF SCOT", "Food", "burgerkg", "You spent £{amount} at");
        """.trimIndent()

        val GREETING_MESSAGE_RULES_4 = """
            INSERT INTO message_rules(id, name, expenditure_name, expenditure_matcher, amount_matcher)
            VALUES(4, "Revolut", "Food", "McDonalds", "Paid €{amount} at");
        """.trimIndent()
    }
}
