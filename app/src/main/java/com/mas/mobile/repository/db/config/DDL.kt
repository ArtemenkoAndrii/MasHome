package com.mas.mobile.repository.db.config

class DDL {
    companion object {
        val AFTER_SPENDING_INSERT = """
            CREATE TRIGGER after_spendings_insert AFTER INSERT ON spendings
            BEGIN
                UPDATE expenditures
                SET fact = fact + NEW.amount
                WHERE id = NEW.expenditure_id;
            END;
        """.trimIndent()

        val AFTER_SPENDING_UPDATE = """
            CREATE TRIGGER after_spendings_update AFTER UPDATE ON spendings
            BEGIN
                UPDATE expenditures
                SET fact = fact + NEW.amount
                WHERE id = NEW.expenditure_id;
            END;
        """.trimIndent()

        val AFTER_EXPENDITURE_INSERT = """
            CREATE TRIGGER after_expenditure_insert AFTER INSERT ON expenditures
            BEGIN
                UPDATE budgets
                SET fact = fact + NEW.fact
                WHERE id = NEW.budget_id;
            END;
        """.trimIndent()

        val AFTER_EXPENDITURE_UPDATE = """
            CREATE TRIGGER after_expenditure_update AFTER UPDATE ON expenditures
            BEGIN
                UPDATE budgets
                SET fact = fact + NEW.fact
                WHERE id = NEW.budget_id;
            END;
        """.trimIndent()
    }
}