package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mas.mobile.domain.budget.ExpenditureRepository
import com.mas.mobile.domain.message.MessageRule
import com.mas.mobile.domain.message.MessageRuleId
import com.mas.mobile.domain.message.MessageRuleRepository
import com.mas.mobile.presentation.viewmodel.validator.Action
import com.mas.mobile.presentation.viewmodel.validator.FieldValidator
import com.mas.mobile.presentation.viewmodel.validator.Validator
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MessageRuleViewModel @AssistedInject constructor(
    expenditureRepository: ExpenditureRepository,
    private val messageRuleRepository: MessageRuleRepository,
    private val fieldValidator: FieldValidator,
    coroutineService: CoroutineService,
    @Assisted private val action: Action,
    @Assisted private val messageRuleId: Int = NEW_ITEM,
)  : ItemViewModel<MessageRule>(coroutineService, messageRuleRepository) {
    override val model: MessageRule = loadModel()

    var sender = MutableLiveData<String>()
    var expenditureMatcher = MutableLiveData<String>()
    var amountMatcher = MutableLiveData<String>()
    var expenditureId: Int = NEW_ITEM
    var expenditureName = MutableLiveData<String>()
    var expenditureNameError = MutableLiveData(Validator.NO_ERRORS)

    val availableExpenditures = expenditureRepository
        .getExpenditureNames(true, 20)
        .map { it.value }
        .toList()

    init {
        initProperties(model)
    }

    private fun loadModel() =
        with(messageRuleRepository) {
            when(action) {
                Action.ADD -> create().also { enableEditing() }
                Action.VIEW -> getById(MessageRuleId(messageRuleId))
                Action.EDIT -> getById(MessageRuleId(messageRuleId)).also { enableEditing() }
                Action.CLONE -> {
                    val origin = getById(MessageRuleId(messageRuleId))
                    enableEditing()
                    create().also {
                        if (origin != null) {
                            it.name = origin.name
                            it.amountMatcher = origin.amountMatcher
                            it.expenditureName = origin.expenditureName
                            it.expenditureMatcher = origin.expenditureMatcher
                        }
                    }
                }
                else -> throw ActionNotSupportedException("MessageRuleViewModel does not support $action action")
            } ?: throw ItemNotFoundException("Item not found id=$messageRuleId")
        }

    private fun initProperties(item: MessageRule) {
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
