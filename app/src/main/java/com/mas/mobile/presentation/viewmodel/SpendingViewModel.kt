package com.mas.mobile.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.SpendingRepository
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.repository.db.entity.Spending
import java.time.LocalDateTime

class SpendingViewModel(application: Application,
                        spendingId: Int = NEW_ITEM,
                        expenditureId: Int = -1,
                        budgetId: Int = -1,
                        editable: Boolean = false): CommonViewModel<Spending>(application, editable) {
    var comment = MutableLiveData<String?>()
    var commentError = MutableLiveData(Validator.NO_ERRORS)
    var date = MutableLiveData<LocalDateTime>()
    var dateError = MutableLiveData(Validator.NO_ERRORS)
    var amount = MutableLiveData<Double>()
    var amountError = MutableLiveData(Validator.NO_ERRORS)
    var expenditureName = MutableLiveData<String>()
    var newExpenditureName: String = ""
    var expenditureId = MutableLiveData<Int>()
    var expenditureIdError = MutableLiveData(Validator.NO_ERRORS)

    val availableExpenditures: LiveData<List<Expenditure>>

    init {
        ExpenditureRepository.initDb(application)
        availableExpenditures = if (budgetId > 0) {
            ExpenditureRepository.live.getByBudgetId(budgetId)
        } else {
            BudgetRepository.initDb(application)
            val id = BudgetRepository.getActive().id
            ExpenditureRepository.live.getByBudgetId(id)
        }

        SpendingRepository.initDb(application)
        val spending = if (spendingId > 0) {
            SpendingRepository.getById(spendingId)
        } else {
            SpendingRepository.createNew(expenditureId)
        }

        liveModel.value = spending
    }

    override fun notifyFields(value: Spending) {
        comment.value = value.comment
        date.value = value.date
        amount.value = value.amount
        expenditureId.value = value.expenditureId
        expenditureName.value = value.expenditure.name
    }

    override fun handleChanges(value: Spending) {
        comment.observeForever {
            value.comment = it
        }
        date.observeForever {
            value.date = it
        }
        amount.observeForever {
            value.amount = it
        }
        expenditureId.observeForever { id ->
            value.expenditureId = id
        }
        expenditureName.observeForever { name ->
            if (name != value.expenditure.name) {
                value.expenditureId = 0
            }
            newExpenditureName = name

            safeValidator {
                Validator(newExpenditureName).minLength(3).message.also{expenditureIdError.value = it}
            }
        }
    }

    override suspend fun doAdd(model: Spending) {
        createExpenditureIfRequired(model)
        SpendingRepository.insert(model)
    }

    override suspend fun doClone(model: Spending) {
        model.id = 0
        createExpenditureIfRequired(model)
        SpendingRepository.insert(model)
    }

    override suspend fun doEdit(model: Spending) {
        createExpenditureIfRequired(model)
        SpendingRepository.update(model)
    }

    override suspend fun doRemove(model: Spending) {
        SpendingRepository.delete(model)
    }

    private suspend fun createExpenditureIfRequired(model: Spending) {
        if (model.expenditureId <= 0 && newExpenditureName.isNotBlank()) {
            Log.d(this::class.simpleName, "Creating new expenditure $newExpenditureName")

            val exp = ExpenditureRepository.createNew().also { it.name = newExpenditureName }
            val result = ExpenditureRepository.insert(exp)
            Log.d(this::class.simpleName, "RESULT $result")

            if (result.isNotEmpty() && result.first() > 0) {
                model.expenditureId = result.first().toInt()
            } else {
                Log.e(this::class.simpleName, "Can not create new expenditure")
            }
        }
    }
}