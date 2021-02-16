package com.mas.mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.db.entity.Budget
import java.time.LocalDate

class BudgetViewModel(application: Application,
                      budgetId: Int,
                      editable: Boolean): CommonViewModel<Budget>(application, editable) {
    val name = MutableLiveData<String>()
    val nameError = MutableLiveData(Validator.NO_ERRORS)
    val plan = MutableLiveData<Double>()
    val fact = MutableLiveData<Double>()
    val startsOn = MutableLiveData<LocalDate>()
    val startsOnError = MutableLiveData(Validator.NO_ERRORS)
    val isActive = MutableLiveData<Boolean>()
    val comment = MutableLiveData<String?>()
    val basedOnId = MutableLiveData<Int>()

    private var basedOnIdValue: Int = -1

    val availableBudgets: LiveData<List<Budget>>

    init {
        BudgetRepository.initDb(application)
        availableBudgets = BudgetRepository.live.getAll()

        val budget = if (budgetId > 0) {
            BudgetRepository.getById(budgetId)
        } else {
            BudgetRepository.createNew()
        }

        liveModel.value = budget
    }

    override fun notifyFields(value: Budget) {
        name.value = value.name
        plan.value = value.plan
        fact.value = value.fact
        startsOn.value = value.startsOn
        isActive.value = value.isActive
        comment.value = value.comment
    }

    override fun handleChanges(value: Budget) {
        name.observeForever{ name ->
            value.name = name
            safeValidator { model ->
                Validator(model.name).minLength(3).message.also { nameError.value = it }
            }
        }
        plan.observeForever{
            value.plan = it
        }
        fact.observeForever{
            value.fact = it
        }
        startsOn.observeForever{ startsOn ->
            value.startsOn = startsOn
            safeValidator { model ->
                Validator(model.startsOn).onlyFuture().message.also { startsOnError.value = it }
            }
        }
        isActive.observeForever{
            value.isActive = it
        }
        comment.observeForever{
            value.comment = it
        }
        basedOnId.observeForever{
            basedOnIdValue = it
        }
    }

    override suspend fun doAdd(model: Budget) {
        val ids = BudgetRepository.insert(model)

        // Not the same transaction!
        if (basedOnIdValue > 0 && ids.size == 1) {
            ExpenditureRepository.getByBudgetId(basedOnIdValue).forEach {
                val clone = it.copy().also { exp ->
                    exp.data.id = 0
                    exp.data.fact = 0.0
                    exp.data.budget_id = ids[0].toInt()
                }

                ExpenditureRepository.insert(clone)
            }

        }
    }

    override suspend fun doClone(model: Budget) {
        model.id = 0
        BudgetRepository.insert(model)
    }

    override suspend fun doEdit(model: Budget) {
        BudgetRepository.update(model)
    }

    override suspend fun doRemove(model: Budget) {
        BudgetRepository.delete(model)
    }

}