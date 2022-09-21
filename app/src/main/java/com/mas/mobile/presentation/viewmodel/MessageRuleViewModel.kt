package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.MessageRuleRepository
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.repository.db.entity.MessageRule
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MessageRuleViewModel @AssistedInject constructor(
    private val expenditureRepository: ExpenditureRepository,
    private val messageRuleRepository: MessageRuleRepository,
    private val fieldValidator: FieldValidator,
    budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted action: Action,
    @Assisted messageRuleId: Int = NEW_ITEM,
) : BaseViewModel<MessageRule>(messageRuleId, action, coroutineService) {
    private val activeBudgetId = budgetService.getActiveOrCreate().id

    var sender = MutableLiveData<String>()
    var expenditureMatcher = MutableLiveData<String>()
    var amountMatcher = MutableLiveData<String>()
    var expenditureId: Int = NEW_ITEM
    var expenditureName = MutableLiveData<String>()
    var expenditureNameError = MutableLiveData(Validator.NO_ERRORS)

    val availableExpenditures: LiveData<List<Expenditure>> =
        expenditureRepository.live.getByBudgetId(this.activeBudgetId)

    init {
        load()
    }

    override fun getRepository() = messageRuleRepository

    override fun afterLoad(item: MessageRule) {
        sender.value = item.name
        sender.observeForever { item.name = it }

        amountMatcher.value = item.amountMatcher
        amountMatcher.observeForever { item.amountMatcher = it }

        expenditureMatcher.value = item.expenditureMatcher
        expenditureMatcher.observeForever { item.expenditureMatcher = it }

        expenditureName.value = item.expenditureName
        expenditureName.observeForever { name ->
            item.expenditureName = name
            validateOnChange(expenditureNameError) {
                fieldValidator.minLength(name.trim(), EXPENDITURE_MIN_LENGTH)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(messageRuleId: Int = NEW_ITEM, action: Action): MessageRuleViewModel
    }

    companion object {
        const val EXPENDITURE_MIN_LENGTH = 3
    }
}
