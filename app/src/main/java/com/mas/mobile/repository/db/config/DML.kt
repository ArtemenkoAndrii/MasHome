package com.mas.mobile.repository.db.config

import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DML {
    companion object {
        private val nowAsLong = SQLiteTypeConverter().fromLocalDate(LocalDate.now())

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
            VALUES(2, "Purchases", 100.00, 0.00, "Automatically generated", 1);
        """.trimIndent()

        val GREETING_MESSAGE_RULES = """
            INSERT INTO message_rules(id, name, expenditure_id, expenditure_matcher, amount_matcher)
            VALUES(1, "Default", 1, "Puzata Khata", "poslugu {amount} UAH");
        """.trimIndent()
    }
}
