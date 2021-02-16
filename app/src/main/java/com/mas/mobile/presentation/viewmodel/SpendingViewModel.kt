package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.presentation.viewmodel.ExpenditureViewModel.Companion.EXPENDITURE_MIN_LENGTH
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.SpendingRepository
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.repository.db.entity.Spending
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDateTime

class SpendingViewModel @AssistedInject constructor(
    private val spendingRepository: SpendingRepository,
    private val expenditureRepository: ExpenditureRepository,
    private val fieldValidator: FieldValidator,
    budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted private val action: Action,
    @Assisted("spendingId") pSpendingId: Int,
    @Assisted("expenditureId") pExpenditureId: Int,
    @Assisted("budgetId") pBudgetId: Int
) : BaseViewModel<Spending>(pSpendingId, action, coroutineService) {
    var comment = MutableLiveData<String?>()
    var commentError = MutableLiveData(Validator.NO_ERRORS)
    var date = MutableLiveData<LocalDateTime>()
    var dateError = MutableLiveData(Validator.NO_ERRORS)
    var amount = MutableLiveData<Double>()
    var amountError = MutableLiveData(Validator.NO_ERRORS)
    var expenditureName = MutableLiveData<String>()
    var expenditureId: Int = pExpenditureId
    var expenditureIdError = MutableLiveData(Validator.NO_ERRORS)

    val availableExpenditures: LiveData<List<Expenditure>>
    private val budgetId = if (pBudgetId == ACTIVE_BUDGET) {
                                budgetService.getActiveOrCreate().id
                            } else {
                                pBudgetId
                            }
    init {
        load()

        availableExpenditures = expenditureRepository.live.getByBudgetId(budgetId)

        if (action == Action.ADD && pExpenditureId > 0) {
            loadSuggestedExpenditure(pExpenditureId)
        }
    }

    override fun getRepository() = spendingRepository

    override fun afterLoad(item: Spending) {
        comment.value = item.comment
        comment.observeForever {
            item.comment = it
        }

        date.value = item.date
        date.observeForever {
            item.date = it
        }

        amount.value = item.amount
        amount.observeForever {
            item.amount = it
        }

        expenditureName.value = item.expenditure.name
        expenditureName.observeForever {
            validateOnChange(expenditureIdError) {
                fieldValidator.minLength(expenditureName.value, EXPENDITURE_MIN_LENGTH)
            }
        }
    }

    override fun beforeSave(item: Spending) {
        if (action == Action.CLONE) {
            expenditureId = item.expenditureId
        }

        if (expenditureId == NEW_ITEM) {
            val newExpenditure = expenditureRepository.createNew().also {
                it.data.name = expenditureName.value ?: ""
                it.data.budget_id = budgetId
            }

            item.expenditure = newExpenditure.data
            item.expenditureId = newExpenditure.data.id
        } else {
            item.expenditureId = expenditureId
        }
    }

    private fun loadSuggestedExpenditure(expenditureId: Int) {
        expenditureRepository.getById(expenditureId)?.let {
            this.expenditureId = expenditureId
            expenditureName.value = it.name
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("spendingId") messageRuleId: Int,
                   @Assisted("expenditureId") expenditureId: Int,
                   @Assisted("budgetId") budgetId: Int,
                   action: Action): SpendingViewModel
    }

    companion object {
        const val ACTIVE_BUDGET = -1
    }
}