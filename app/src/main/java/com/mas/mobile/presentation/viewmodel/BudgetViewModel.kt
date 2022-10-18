package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDate

class BudgetViewModel @AssistedInject constructor(
    private val budgetRepository: BudgetRepository,
    private val fieldValidator: FieldValidator,
    private val budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted private val action: Action,
    @Assisted private val budgetId: Int = NEW_ITEM,
) : BaseViewModel<Budget>(budgetId, action, coroutineService) {
    val name = MutableLiveData<String>()
    val nameError = MutableLiveData(Validator.NO_ERRORS)
    val plan = MutableLiveData<Double>()
    val fact = MutableLiveData<Double>()
    val startsOn = MutableLiveData<LocalDate>()
    val startsOnError = MutableLiveData(Validator.NO_ERRORS)
    val lastDayAt = MutableLiveData<LocalDate>()
    val lastDayAtError = MutableLiveData(Validator.NO_ERRORS)
    val comment = MutableLiveData<String?>()

    var startsOnValue: LocalDate = LocalDate.now()
    var lastDayAtValue: LocalDate = LocalDate.now()

    init {
        load()
    }

    override fun afterLoad(item: Budget) {
        name.value = item.name
        name.observeForever {
            item.name = it
            validateOnChange(nameError) {
                fieldValidator.minLength(name.value, EXPENDITURE_MIN_LENGTH)
            }
        }

        plan.value = item.plan
        plan.observeForever {
            item.plan = it
        }

        fact.value = item.fact
        fact.observeForever {
            item.fact = it
        }

        startsOn.value = item.startsOn
        startsOn.observeForever {
            item.startsOn = it
            startsOnValue = it
        }

        lastDayAt.value = item.lastDayAt
        lastDayAt.observeForever {
            item.lastDayAt = it
            lastDayAtValue = it
        }

        comment.value = item.comment
        comment.observeForever {
            item.comment = it
        }
    }

    override fun getRepository() = budgetRepository

    fun isChangeable() =
        budgetRepository.getLastCompletedOn(LocalDate.MAX)?.let {
            it.id == budgetId
        } ?: true

    override fun afterRemove() {
        // Just for case if the only budget will be removed
        budgetService.reloadBudget()
    }

    @AssistedFactory
    interface Factory {
        fun create(budget: Int = NEW_ITEM, action: Action): BudgetViewModel
    }

    companion object {
        const val TEMPLATE_BUDGET_ID = 1
        const val EXPENDITURE_MIN_LENGTH = 3
    }
}
