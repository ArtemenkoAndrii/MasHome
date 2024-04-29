package com.mas.mobile.presentation.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mas.mobile.domain.Repository
import com.mas.mobile.domain.budget.*
import com.mas.mobile.domain.message.*
import com.mas.mobile.presentation.viewmodel.ExpenditureViewModel.Companion.EXPENDITURE_MIN_LENGTH
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Currency

class SpendingViewModel @AssistedInject constructor(
    coroutineService: CoroutineService,
    private val budgetService: BudgetService,
    private val spendingRepository: SpendingRepository,
    private val expenditureRepository: ExpenditureRepository,
    private val fieldValidator: FieldValidator,
    private val messageRuleService: MessageRuleService,
    private val messageService: MessageService,
    @Assisted private val action: Action,
    @Assisted("spendingId") private val pSpendingId: Int,
    @Assisted("expenditureId") private val pExpenditureId: Int,
    @Assisted("budgetId") private val pBudgetId: Int,
    @Assisted("messageId") private val pMessageId: Int,
) : ItemViewModel<Spending>(coroutineService, SpendingRepositoryAdapter(budgetService, BudgetId(pBudgetId))) {
    private var changedRule: MessageRule? = null

    val budget: Budget = loadBudget()
    override val model: Spending = loadModel()

    val discoverMode = MutableLiveData<Boolean>()
    var message: Message? = loadMessage()

    var comment = MutableLiveData<String>()
    var commentError = MutableLiveData(Validator.NO_ERRORS)
    var date = MutableLiveData<LocalDateTime>()
    var dateError = MutableLiveData(Validator.NO_ERRORS)
    var amount = MutableLiveData<Double>()
    var amountError = MutableLiveData(Validator.NO_ERRORS)
    var expenditureName = MutableLiveData<String>()
    var expenditureNameError = MutableLiveData(Validator.NO_ERRORS)

    var exchangeAmount = MutableLiveData<Double>()
    var exchangeRate = MutableLiveData<Double>()
    var exchangeCurrency = MutableLiveData<Currency>()
    val exchangeVisibility = Transformations.map(exchangeCurrency) {
        if (model.exchangeInfo != null) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private var expenditureNameValue = ""

    val availableExpenditures = expenditureRepository
        .getExpenditureNames(true, 20)
        .map { it.value }
        .toList()

    fun discover(handleResult: (success: Boolean) -> Unit) {
        message?.let {
            viewModelScope.launch {
                handleResult(doDiscover(it))
            }
        }
    }

    fun hasChangedRule(): Boolean {
        val candidate = changedRule ?: (message?.status as? Message.Matched)?.ruleId?.let { ruleId ->
            messageRuleService.ruleRepository.getById(ruleId)
        }

        changedRule = messageRuleService.evaluateRuleChanges(message, candidate, expenditureNameValue)

        return changedRule != null
    }

    fun resetRuleChanges() {
        changedRule = null
    }

    init {
        if (action != Action.VIEW) enableEditing()
        initProperties()
        initDefaultExpenditure(ExpenditureId(pExpenditureId))
    }

    private fun loadBudget() = budgetService.loadBudgetOrGetActive(BudgetId(pBudgetId))

    private fun loadModel(): Spending =
        when(action) {
            Action.ADD -> spendingRepository.create()
            Action.VIEW, Action.EDIT -> spendingRepository.getSpending(SpendingId(pSpendingId))
            Action.CLONE -> {
                spendingRepository.create().also {
                    val origin = spendingRepository.getSpending(SpendingId(pSpendingId))
                    if (origin != null) {
                        it.amount = origin.amount
                        it.date = LocalDateTime.now()
                        it.comment = origin.comment
                        it.expenditure = findOrCreateExpenditure(origin.expenditure.name)
                        it.exchangeInfo = origin.exchangeInfo
                    }
                }
            }
            else -> throw ActionNotSupportedException("BudgetViewModel does not support $action action")
        } ?: throw ItemNotFoundException("Item not found id=$pBudgetId")

    private fun loadMessage(): Message? =
        if (pMessageId > 0) {
            messageService.messageRepository.getById(MessageId(pMessageId))
        } else {
            messageService.messageRepository.getBySpendingId(SpendingId(pSpendingId))
        }

    private fun initProperties() {
        comment.value = model.comment
        comment.observeForever {
            model.comment = it
        }

        date.value = model.date
        date.observeForever {
            model.date = it
        }

        amount.value = model.amount
        amount.observeForever {
            model.amount = it
        }

        expenditureName.value = model.expenditure.name
        expenditureName.observeForever {
            expenditureNameValue = it.trim()
            validateOnChange(expenditureNameError) {
                fieldValidator.minLength(expenditureName.value, EXPENDITURE_MIN_LENGTH)
            }
        }

        message?.let {
            if (this.action == Action.ADD) {
                if (it.status is Message.Matched) {
                    amount.value = (it.status as Message.Matched).suggestedAmount
                    comment.value = it.text
                } else {
                    discoverMode.value = true
                }
            }
        }

        exchangeCurrency.value = model.exchangeInfo?.currency ?: budget.currency
        exchangeCurrency.observeForever {
            if (it != budget.currency) {
                if (model.exchangeInfo == null) {
                    model.exchangeInfo = ExchangeInfo(
                        rawAmount = model.amount,
                        rate = budgetService.getRate(it),
                        currency = it
                    )
                } else {
                    model.exchangeInfo?.rate = budgetService.getRate(it)
                    model.exchangeInfo?.currency = it
                }
                model.exchange()
                exchangeRate.value = model.exchangeInfo?.rate ?: ZERO
                exchangeAmount.value = model.exchangeInfo?.rawAmount ?: ZERO
            } else {
                amount.value = model.exchangeInfo?.rawAmount ?: model.amount
                model.exchangeInfo = null
            }
        }

        exchangeAmount.value = model.exchangeInfo?.rawAmount ?: ZERO
        exchangeAmount.observeForever {
            model.exchangeInfo?.rawAmount = it
            model.exchange()
            amount.value = model.amount
        }

        exchangeRate.value = model.exchangeInfo?.rate ?: ZERO
        exchangeRate.observeForever {
            model.exchangeInfo?.rate = it
            model.exchange()
            exchangeAmount.value = model.exchangeInfo?.rawAmount ?: ZERO
        }
    }

    private suspend fun doDiscover(message: Message): Boolean {
        val rule = messageRuleService.generateRuleFromMessage(message)

        return when (val result = rule?.evaluate(message.sender, message.text)) {
            is MessageRule.Match -> {
                this.discoverMode.value = false
                this.changedRule = rule
                this.message = message.toMatched(rule.id, result.amount, "")

                comment.value = message.text
                date.value = message.receivedAt
                expenditureName.value = ""
                amount.value = result.amount
                exchangeCurrency.value = rule.currency
                true
            }
            else -> {
                this.changedRule = null
                false
            }
        }
    }

    override suspend fun doSave() {
        model.expenditure = findOrCreateExpenditure(expenditureNameValue)

        super.doSave()

        saveDependencies()
    }

    private fun findOrCreateExpenditure(name: String) =
        budget.findExpenditure(name) ?: expenditureRepository.create().also { it.name = name }

    private fun initDefaultExpenditure(expenditureId: ExpenditureId) {
        if (action == Action.ADD && expenditureId.value > 0) {
            expenditureName.value = budget.getExpenditure(expenditureId)?.name ?: ""
        }
    }

    private suspend fun saveDependencies() {
        changedRule?.let {
            it.expenditureName = model.expenditure.name
            it.currency = model.exchangeInfo?.currency ?: budget.currency
            messageRuleService.ruleRepository.save(it)
        }

        message?.let {
            it.spendingId = model.id
            messageService.messageRepository.save(it)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("spendingId") spendingId: Int,
                   @Assisted("expenditureId") expenditureId: Int,
                   @Assisted("budgetId") budgetId: Int,
                   @Assisted("messageId") messageId: Int,
                   action: Action): SpendingViewModel
    }

    companion object {
        private const val ZERO = 0.00
    }
}

class SpendingRepositoryAdapter(val service: BudgetService, budgetId: BudgetId) : Repository<Spending> {
    private val budget: Budget = service.loadBudgetOrGetActive(budgetId)

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
