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
import com.mas.mobile.util.Analytics
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
    private val messageService: MessageService,
    private val categoryService: CategoryService,
    private val messageTemplateService: MessageTemplateService,
    private val analytics: Analytics,
    @Assisted private val action: Action,
    @Assisted("spendingId") private val pSpendingId: Int,
    @Assisted("expenditureId") private val pExpenditureId: Int,
    @Assisted("budgetId") private val pBudgetId: Int,
    @Assisted("messageId") private val pMessageId: Int,
) : ItemViewModel<Spending>(coroutineService, SpendingRepositoryAdapter(budgetService, BudgetId(pBudgetId))) {
    private var discoveredMessageTemplate: MessageTemplate? = null
    private var category: Category? = null

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

    /*
        1. If the spending is created based on a matched message that has a merchant but does not have spendingId populated.
        2. If the spending is created based on a discovered message template.
    */
    fun hasChangedRule(): Boolean {
        if (action == Action.ADD) {
            if (discoveredMessageTemplate != null) {
                return true
            }

            if (message.getMerchantToSave() != null) {
                return true
            }
        }
        return false
    }

    private fun Message?.getMerchantToSave(): Merchant? {
        val merchant = (this?.status as? Message.Matched)?.merchant ?: return null
        return if (this.spendingId == null && categoryService.findCategoryByMerchant(merchant) == null) {
            merchant
        } else {
            null
        }
    }

    fun resetRuleChanges() {
        discoveredMessageTemplate = null
        category = null
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
            messageService.repository.getById(MessageId(pMessageId))
        } else {
            messageService.repository.getBySpendingId(SpendingId(pSpendingId))
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
                    amount.value = (it.status as Message.Matched).amount
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
        val newTemplate = messageTemplateService.generateTemplateFromMessage(message) ?: return false
        val promotedMessage = messageService.promoteRecommendedMessage(message, newTemplate) ?: return false
        val promotedStatus = promotedMessage.status as? Message.Matched ?: return false

        this.discoveredMessageTemplate = newTemplate
        this.discoverMode.value = false
        this.message = promotedMessage

        comment.value = message.text
        date.value = message.receivedAt
        expenditureName.value = ""
        amount.value = promotedStatus.amount
        exchangeCurrency.value = newTemplate.currency

        return true
    }

    override suspend fun doSave() {
        beforeSave()
        super.doSave()
        saveDependencies()
        logEvent()
    }

    private fun findOrCreateExpenditure(name: String) =
        budget.findExpenditure(name) ?: expenditureRepository.create().also { it.name = name }

    private fun initDefaultExpenditure(expenditureId: ExpenditureId) {
        if (action == Action.ADD && expenditureId.value > 0) {
            expenditureName.value = budget.getExpenditure(expenditureId)?.name ?: ""
        }
    }

    private fun beforeSave() {
        model.expenditure = findOrCreateExpenditure(expenditureNameValue)

        val merchant = message.getMerchantToSave()
        if (merchant != null) {
            this.category = categoryService.findCategoryByName(expenditureNameValue)?.also {
                it.merchants.add(merchant)
            }
        }
    }

    private suspend fun saveDependencies() {
        message?.let {
            it.spendingId = model.id
            messageService.repository.save(it)
        }

        discoveredMessageTemplate?.let {
            messageTemplateService.repository.save(it)
        }

        category?.let {
            categoryService.repository.save(it)
        }
    }

    private fun logEvent() {
        if (action == Action.ADD) {
            val source = if (message != null) {
                "message"
            } else {
                "form"
            }
            analytics.logEvent(Analytics.Event.SPENDING_CREATED, Analytics.Param.SOURCE, source)
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
