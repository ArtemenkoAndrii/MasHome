package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mas.mobile.model.SpendingMessageEnvelop
import com.mas.mobile.presentation.viewmodel.ExpenditureViewModel.Companion.EXPENDITURE_MIN_LENGTH
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.SpendingMessageRepository
import com.mas.mobile.repository.SpendingRepository
import com.mas.mobile.repository.db.entity.Spending
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import com.mas.mobile.service.ExpenditureService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDateTime

class SpendingViewModel @AssistedInject constructor(
    private val spendingRepository: SpendingRepository,
    private val expenditureService: ExpenditureService,
    private val budgetService: BudgetService,
    private val spendingMessageRepository: SpendingMessageRepository,
    private val fieldValidator: FieldValidator,
    private val coroutineService: CoroutineService,
    @Assisted private val action: Action,
    @Assisted("spendingId") pSpendingId: Int,
    @Assisted("expenditureId") pExpenditureId: Int,
    @Assisted("budgetId") pBudgetId: Int,
    @Assisted("envelop") private val envelop: String,
) : BaseViewModel<Spending>(pSpendingId, action, coroutineService) {
    var comment = MutableLiveData<String?>()
    var commentError = MutableLiveData(Validator.NO_ERRORS)
    var date = MutableLiveData<LocalDateTime>()
    var dateError = MutableLiveData(Validator.NO_ERRORS)
    var amount = MutableLiveData<Double>()
    var amountError = MutableLiveData(Validator.NO_ERRORS)
    var expenditureName = MutableLiveData<String>()
    var expenditureNameError = MutableLiveData(Validator.NO_ERRORS)
    private var expenditureNameValue = ""

    private val budgetId = if (pBudgetId == ACTIVE_BUDGET) {
                                budgetService.getActiveOrCreate().id
                            } else {
                                pBudgetId
                            }
    val availableExpenditures = expenditureService.expenditureRepository.live.getByBudgetId(budgetId)

    init {
        load()
        initDefaultExpenditure(pExpenditureId)
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
            expenditureNameValue = it
            validateOnChange(expenditureNameError) {
                fieldValidator.minLength(expenditureName.value, EXPENDITURE_MIN_LENGTH)
            }
        }

        if (this.action == Action.ADD) {
            getDependantSpendingMessage()?.let {
                amount.value = it.amount
                comment.value = it.text
            }
        }
    }

    override fun beforeSave(item: Spending) {
        if (action == Action.ADD || action == Action.EDIT) {
            item.expenditureId = expenditureService.findOrCreate(expenditureNameValue, budgetId).id
        }
    }

    override suspend fun afterSave(item: Spending) {
        budgetService.calculateBudget(budgetId)

        getDependantSpendingMessage()?.let {
            val message = spendingMessageRepository.getById(it.id)
            if (message != null) {
                message.spendingId = item.id
                message.suggestedExpenditureName = item.expenditure.name
                spendingMessageRepository.update(message)
            }
        }
    }

    override suspend fun afterRemove(item: Spending) {
        budgetService.calculateBudget(item.expenditure.budget_id)
    }

    private fun initDefaultExpenditure(expenditureId: Int) {
        if (action == Action.ADD && expenditureId > 0) {
            expenditureName.value = expenditureService.expenditureRepository.getById(expenditureId)?.name ?: ""
        }
    }

    private fun getDependantSpendingMessage() = SpendingMessageEnvelop.fromString(this.envelop)

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("spendingId") messageRuleId: Int,
                   @Assisted("expenditureId") expenditureId: Int,
                   @Assisted("budgetId") budgetId: Int,
                   @Assisted("envelop") envelop: String,
                   action: Action): SpendingViewModel
    }

    companion object {
        const val ACTIVE_BUDGET = -1
    }
}