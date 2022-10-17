package com.mas.mobile.service

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoroutineService @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    fun backgroundTask(wrapper: suspend (context: CoroutineScope) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch(coroutineErrorHandler) {
            wrapper(this)
        }
    }

    private val coroutineErrorHandler = CoroutineExceptionHandler { _, throwable ->
        errorHandler.handleAndNotify(throwable)
    }
}
