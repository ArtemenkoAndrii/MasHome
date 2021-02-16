package com.mas.mobile.repository.db.config

import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DML {
    companion object {
        private val nowAsMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))
        private val nowAsLong = SQLiteTypeConverter().fromLocalDate(LocalDate.now())

        val GREETING_BUDGET = """
            INSERT INTO budgets(id, name, startsOn, plan, fact, isActive, comment)
            VALUES(1, "$nowAsMonthYear", $nowAsLong, 0.00, 0.00, 1, "Automatically generated");
        """.trimIndent()

        val GREETING_EXPENDITURE = """
            INSERT INTO expenditures(id, name, plan, fact, comment, budget_id)
            VALUES(1, "Purchases", 0.00, 0.00, "Automatically generated", 1);
        """.trimIndent()

        val GREETING_SPENDING = """
            INSERT INTO spendings(id, comment, date, amount, expenditure_id)
            VALUES(1, "Automatically generated", $nowAsLong, 0.00, 1);
        """.trimIndent()
    }
}
