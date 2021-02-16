package com.mas.mobile.presentation.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.R
import com.mas.mobile.presentation.viewmodel.validator.Action
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val EDITABLE = true
const val NEW_ITEM = -1

open class CommonViewModel<T: Any>(application: Application,
                           editable: Boolean = false): AndroidViewModel(application) {

    var isEditable = MutableLiveData(true)

    protected val liveModel = MutableLiveData<T>()

    private lateinit var model: T
    private var validationLock: Boolean = true
    private val validators = mutableSetOf<Validator<T>>()

    fun enableEditing() {
        isEditable.value = true
    }

    init {
        liveModel.observeForever{
            model = it
            notifyFields(model)
            handleChanges(model)
            validationLock = false
        }
        isEditable.value = editable
    }

    fun save(action: Action, confirm: (value: T) -> Boolean = { true }): Boolean {
        val isValid = validate()

        if (isValid && confirm(model)) {
            when (action) {
                Action.ADD -> asyncCall(model, ::doAdd)
                Action.CLONE -> asyncCall(model, ::doClone)
                Action.EDIT -> asyncCall(model, ::doEdit)
                Action.REMOVE -> asyncCall(model, ::doRemove)
                Action.VIEW -> Log.w(this::class.simpleName, "View is not supported action")
            }
        }

        return isValid
    }

    protected fun safeValidator(validator: (model: T) -> String): Boolean {
        return if (!validationLock) {
            return validator(model).isEmpty()
        } else {
            validators.add(validator)
            if (validators.size > 50) {
                Log.w(this::class.simpleName, "Attention! Form validators leaking is detected!")
            }
            true
        }
    }

    protected open fun notifyFields(value: T) {}
    protected open fun handleChanges(value: T) {}
    protected open suspend fun doAdd(model: T) {}
    protected open suspend fun doClone(model: T) {}
    protected open suspend fun doEdit(model: T) {}
    protected open suspend fun doRemove(model: T) {}

    protected open fun validate(): Boolean {
        return !validators.map{ v -> v(model) }.any{ e -> e.isNotEmpty() }
    }

    private val errorHandler = CoroutineExceptionHandler {_, throwable ->
        val message = application.applicationContext.getString(R.string.unexpected_error)
        Toast.makeText(application, throwable.message, Toast.LENGTH_SHORT).show()
        Log.e(this::class.simpleName, message, throwable)
    }

    private fun asyncCall(param: T, act: suspend(param: T) -> Unit) {
        GlobalScope.launch(errorHandler) {
            act(param)
        }
    }
}

typealias Validator<T> = (model: T) -> String