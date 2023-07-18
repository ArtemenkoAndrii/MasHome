package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

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
    @Assisted("envelop") private val envelop: String,
) : ItemViewModel<Spending>(coroutineService, SpendingRepositoryAdapter(budgetService, BudgetId(pBudgetId))) {
    private var newRule: MessageRule? = null
    private val budget: Budget = loadBudget()
    override val model: Spending = loadModel()

    val messageDrivenMode = MutableLiveData<Boolean>()
    var message: Message? = null

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

    fun processMessage(onDone: (success: Boolean) -> Unit) {
        this.message?.let {
            viewModelScope.launch {
                onDone(doProcess(it))
            }
        }
    }

    fun newRule(): Boolean = newRule?.let { rule ->
        !messageRuleService.ruleRepository.getAll()
            .filter { it.name.lowercase() == rule.name.lowercase() }
            .any { it.expenditureName.lowercase() == expenditureNameValue.lowercase() }
    } ?: false

    fun saveRule() = runBlocking {
        newRule?.let {
            it.expenditureName = expenditureNameValue
            messageRuleService.ruleRepository.save(it)
        }
    }

    init {
        if (action != Action.VIEW) enableEditing()
        initProperties(model)
        initDefaultExpenditure(ExpenditureId(pExpenditureId))
    }

    private fun loadBudget(): Budget {
        if (action == Action.ADD || action == Action.CLONE) {
            pBudgetId
        } else {
            spendingRepository.getSpending(SpendingId(pSpendingId))?.expenditure?.budgetId
        }
        return budgetService.loadBudgetOrGetActive(BudgetId(pBudgetId))
    }

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
            expenditureNameValue = it.trim()
            validateOnChange(expenditureNameError) {
                fieldValidator.minLength(expenditureName.value, EXPENDITURE_MIN_LENGTH)
            }
        }

        if (this.action == Action.ADD) {
            getDependantSpendingMessage()?.let {
                amount.value = it.amount
                comment.value = it.text

                message = messageService.messageRepository.getById(MessageId(it.id))
                messageDrivenMode.value = message?.status == Message.Status.RECOMMENDED
            }
        }
    }

    private suspend fun doProcess(message: Message) =
        messageRuleService.buildRule(message.sender, message.text)?.let {
            when(val result = messageService.classify(message.sender, message.text, listOf(it))) {
                is MessageService.Captured -> {
                    this.newRule = it
                    messageDrivenMode.value = false
                    comment.value = message.text
                    date.value = message.receivedAt
                    amount.value = result.amount
                    expenditureName.value = result.expenditureName ?: ""
                    true
                }
                else -> false
            }
        } ?: false

    override suspend fun doSave() {
        if (action == Action.ADD || action == Action.EDIT) {
            model.expenditure = findOrCreateExpenditure(expenditureNameValue)
        }

        super.doSave()

        markMessageAssigned(model)
    }

    private fun findOrCreateExpenditure(name: String) =
        budget.findExpenditure(name) ?: expenditureRepository.create().also { it.name = name }

    private suspend fun markMessageAssigned(item: Spending) {
        getDependantSpendingMessage()?.let {
            val message = messageService.messageRepository.getById(MessageId(it.id))
            if (message != null) {
                message.spendingId = item.id
                message.status = Message.Status.MATCHED
                message.suggestedExpenditureName = item.expenditure.name
                messageService.messageRepository.save(message)
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
