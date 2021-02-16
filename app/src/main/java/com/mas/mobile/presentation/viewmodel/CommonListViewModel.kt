package com.mas.mobile.presentation.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.mas.mobile.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class CommonListViewModel<T: Any>(application: Application) : AndroidViewModel(application){
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        val message = application.applicationContext.getString(R.string.unexpected_error)
        Toast.makeText(application, throwable.message, Toast.LENGTH_SHORT).show()
        Log.e(this::class.simpleName, message, throwable)
    }

    protected fun doRemove(item: T, act: suspend(param: T) -> Unit) {
        GlobalScope.launch(errorHandler) {
            act(item)
        }
    }

}