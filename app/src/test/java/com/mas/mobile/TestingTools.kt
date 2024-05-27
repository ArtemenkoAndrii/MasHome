package com.mas.mobile

import com.mas.mobile.service.TaskService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest

object DummyTaskService : TaskService {

    override fun backgroundTask(wrapper: suspend (context: CoroutineScope) -> Unit) {
        runTest {
            wrapper(this)
        }
    }

    override fun blockingTask(wrapper: suspend (context: CoroutineScope) -> Unit) {
        runTest {
            wrapper(this)
        }
    }
}