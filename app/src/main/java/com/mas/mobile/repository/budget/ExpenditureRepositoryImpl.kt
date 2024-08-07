package com.mas.mobile.repository.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mas.mobile.domain.budget.*
import com.mas.mobile.repository.db.config.AppDatabase
import javax.inject.Singleton

@Singleton
class ExpenditureRepositoryImpl(
    val db: AppDatabase
) : ExpenditureRepository {
    override fun create(): Expenditure =
        Expenditure(
            id = ExpenditureId(db.idGeneratorDAO().generateId().toInt()),
            name = "",
            categoryId = CategoryId(-1),
            plan = 0.0,
            fact = 0.0,
            comment = "",
            budgetId = BudgetId(-1)
        )

    override fun getExpenditureNames(sortByRating: Boolean, limit: Short): Set<ExpenditureName> =
        db.expenditureDao().getUniqueNames().map { ExpenditureName(it) }.toSet()

    override fun getLiveExpenditures(budgetId: BudgetId): LiveData<List<Expenditure>> =
        Transformations.map(db.expenditureDao().getByBudgetIdLive(budgetId.value)) { list ->
            list.map { it.data.toModel() }.toList()
        }

    override fun getExpenditures(budgetId: BudgetId): List<Expenditure> =
        db.expenditureDao().getByBudgetId(budgetId.value).map { it.data.toModel() }.toList()

    override fun getExpenditure(expenditureId: ExpenditureId): Expenditure? =
        db.expenditureDao().getById(expenditureId.value)?.data?.toModel()
}