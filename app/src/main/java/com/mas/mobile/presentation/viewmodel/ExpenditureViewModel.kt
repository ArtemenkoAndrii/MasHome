package com.mas.mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.db.entity.Expenditure

class ExpenditureViewModel(application: Application,
                           expenditureId: Int,
                           budgetId: Int,
                           editable: Boolean): CommonViewModel<Expenditure>(application, editable) {
    val name = MutableLiveData<String>()
    val nameError = MutableLiveData(Validator.NO_ERRORS)
    val plan = MutableLiveData<Double>()
    val fact = MutableLiveData<Double>()
    val comment = MutableLiveData<String?>()

    val availableExpenditures: LiveData<List<Expenditure>>

    init {
        ExpenditureRepository.initDb(application)

        availableExpenditures = ExpenditureRepository.live.getUniqueNames()

        val expenditure = if (expenditureId > 0) {
            ExpenditureRepository.getById(expenditureId)
        } else {
            ExpenditureRepository.createNew(budgetId)
        }
        liveModel.value = expenditure
    }

    override fun notifyFields(value: Expenditure) {
        name.value = value.name
        plan.value = value.plan
        fact.value = value.fact
        comment.value = value.comment
    }

    override fun handleChanges(value: Expenditure) {
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
        comment.observeForever{
            value.comment = it
        }
    }

    override suspend fun doAdd(model: Expenditure) {
        ExpenditureRepository.insert(model)
    }

    override suspend fun doClone(model: Expenditure) {
        model.id = 0
        ExpenditureRepository.insert(model)
    }

    override suspend fun doEdit(model: Expenditure) {
        ExpenditureRepository.update(model)
    }

    override suspend fun doRemove(model: Expenditure) {
        ExpenditureRepository.delete(model)
    }
}