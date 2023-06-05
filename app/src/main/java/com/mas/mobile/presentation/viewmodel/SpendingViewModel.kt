package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mas.mobile.domain.Repository
import com.mas.mobile.domain.budget.*
import com.mas.mobile.domain.message.MessageId
import com.mas.mobile.domain.message.MessageRepository
import com.mas.mobile.domain.budget.SpendingMessageEnvelop
import com.mas.mobile.presentation.viewmodel.ExpenditureViewModel.Companion.EXPENDITURE_MIN_LENGTH
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDateTime

class SpendingViewModel @AssistedInject constructor(
    budgetService: BudgetService,
    coroutineService: CoroutineService,
    private val spendingRepository: SpendingRepository,
    private val expenditureRepository: ExpenditureRepository,
    private val messageRepository: MessageRepository,
    private val fieldValidator: FieldValidator,
    @Assisted private val action: Action,
    @Assisted("spendingId") private val pSpendingId: Int,
    @Assisted("expenditureId") private val pExpenditureId: Int,
    @Assisted("budgetId") private val pBudgetId: Int,
    @Assisted("envelop") private val envelop: String,
) : ItemViewModel<Spending>(coroutineService, SpendingRepositoryAdapter(budgetService, BudgetId(pBudgetId))) {
    private val budget = budgetService.loadBudget(BudgetId(pBudgetId))
    override val model: Spending = loadModel()

    var comment = MutableLiveData<String>()
    var commentError = MutableLiveData(Validator.NO_ERRORS)
    var date = MutableLiveData<LocalDateTime>()
    var dateError = MutableLiveData(Validator.NO_ERRORS)
    var amount = MutableLiveData<Double>()
    var amountError = MutableLiveData(Validator.NO_ERRORS)
    var expenditureName = MutableLiveData<String>()
    var expenditureNameError = MutableLiveData(Validator.NO_ERRORS)
    private var expenditureNameValue = ""

    val availableExpenditures = expenditureRepository
        .getExpenditureNames(true, 20)
        .map { it.value }
        .toList()

    init {
        initProperties(model)
        initDefaultExpenditure(ExpenditureId(pExpenditureId))
    }

    private fun loadModel(): Spending =
        when(action) {
            Action.ADD -> spendingRepository.create().also { enableEditing() }
            Action.VIEW -> budget.getSpending(SpendingId(pSpendingId))
            Action.EDIT -> budget.getSpending(SpendingId(pSpendingId)).also { enableEditing() }
            Action.CLONE -> {
                enableEditing()
                spendingRepository.create().also {
                    val origin = budget.getSpending(SpendingId(pSpendingId))
                    if (origin != null) {
                        it.amount = origin.amount
                        it.date = LocalDateTime.now()
                        it.comment = origin.comment
                        it.expenditure = origin.expenditure
                    }
                }
            }
            else -> throw ActionNotSupportedException("BudgetViewModel does not support $action action")
        } ?: throw ItemNotFoundException("Item not found id=$pBudgetId")


    private fun initProperties(item: Spending) {
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

    override suspend fun doSave() {
        if (action == Action.ADD || action == Action.EDIT) {
            model.expenditure = findOrCreate()
        }

        super.doSave()

        markMessageAssigned(model)
    }

    private fun findOrCreate(): Expenditure {
        return budget.findExpenditure(expenditureNameValue)
            ?: createExpenditure(expenditureNameValue.trim())
    }

    private fun createExpenditure(name: String): Expenditure =
        expenditureRepository.create().also {
            it.name = name
        }

    suspend fun markMessageAssigned(item: Spending) {
        getDependantSpendingMessage()?.let {
            val message = messageRepository.getById(MessageId(it.id))
            if (message != null) {
                message.spendingId = item.id
                message.suggestedExpenditureName = item.expenditure.name
                messageRepository.save(message)
            }
        }
    }

    private fun initDefaultExpenditure(expenditureId: ExpenditureId) {
        if (action == Action.ADD && expenditureId.value > 0) {
            expenditureName.value = budget.getExpenditure(expenditureId)?.name ?: ""
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
}

class SpendingRepositoryAdapter(val service: BudgetService, budgetId: BudgetId) : Repository<Spending> {
    private val budget: Budget = service.loadBudget(budgetId)

    override suspend fun save(item: Spending) {
        if (!hasExpenditure(item.expenditure) ){
            budget.addExpenditure(item.expenditure)
        }

        budget.addSpending(item)
        service.budgetRepository.save(budget)
    }

    override suspend fun remove(item: Spending) {
        budget.removeSpending(item)
        service.budgetRepository.save(budget)
    }

    private fun hasExpenditure(expenditure: Expenditure) =
        budget.budgetDetails.expenditure.any { it.id == expenditure.id }
}