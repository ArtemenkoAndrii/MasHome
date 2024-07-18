package com.mas.mobile.presentation.viewmodel

import com.mas.mobile.domain.budget.Category
import com.mas.mobile.domain.budget.CategoryRepository
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class CategoryListViewModel @AssistedInject constructor(
    coroutineService: CoroutineService,
    repository: CategoryRepository,
    @Assisted("budgetId") budgetId: Int = -1,
) : ListViewModel<Category>(coroutineService, repository) {
    val categories = repository.live.getAll()

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("budgetId") budgetId: Int = -1
        ): CategoryListViewModel
    }
}