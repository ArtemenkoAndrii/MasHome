package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import com.mas.mobile.service.ExpenditureService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ExpenditureViewModel @AssistedInject constructor(
    private val expenditureService: ExpenditureService,
    private val budgetService: BudgetService,
    private val fieldValidator: FieldValidator,
    coroutineService: CoroutineService,
    @Assisted("expenditureId") expenditureId: Int,
    @Assisted("budgetId") private val budgetId: Int,
    @Assisted private val action: Action,
) : BaseViewModel<Expenditure>(expenditureId, action, coroutineService) {
    private var originalName = ""

    val name = MutableLiveData<String>()
    val nameError = MutableLiveData(Validator.NO_ERRORS)
    val plan = MutableLiveData<Double>()
    val fact = MutableLiveData<Double>()
    val comment = MutableLiveData<String?>()

    val availableExpenditures = expenditureService.expenditureRepository.live.getUniqueNames()

    init {
        load()
    }

    override fun afterLoad(item: Expenditure) {
        originalName = item.name
        name.value = item.name
        name.observeForever {
            item.name = it.trim()
            validateOnChange(nameError) {
                fieldValidator.minLength(item.name, EXPENDITURE_MIN_LENGTH)
            }
        }

        plan.value = item.plan
        plan.observeForever{
            item.plan = it
        }

        fact.value = item.fact
        fact.observeForever{
            item.fact = it
        }

        comment.value = item.comment
        comment.observeForever{
            item.comment = it
        }

        validateOnSave(nameError) {
            fieldValidator.alreadyExists(ifExpenditureExists(name.value))
        }
    }

    override fun beforeSave(item: Expenditure) {
        item.data.budget_id = budgetId
    }

    override suspend fun afterSave(item: Expenditure) {
        budgetService.calculateBudget(item.budget.id)
    }

    override suspend fun afterRemove(item: Expenditure) {
        budgetService.calculateBudget(item.budget.id)
    }

    override fun getRepository() = expenditureService.expenditureRepository

    private fun ifExpenditureExists(name: String?): Boolean {
        return if (name != originalName || action == Action.ADD) {
            expenditureService.find(name ?: "", budgetId) != null
        } else {
            false
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("expenditureId") expenditureId: Int,
                   @Assisted("budgetId") budgetId: Int,
                   action: Action): ExpenditureViewModel
    }

    companion object {
        const val EXPENDITURE_MIN_LENGTH = 3
    }
}
