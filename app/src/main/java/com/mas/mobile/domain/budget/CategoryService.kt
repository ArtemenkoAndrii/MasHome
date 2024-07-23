package com.mas.mobile.domain.budget

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryService @Inject constructor(
    val repository: CategoryRepository
) {
    fun findCategoryByMerchant(merchant: Merchant): Category? {
        return repository.getAll()
            .firstOrNull { it.merchants.containsIgnoreCase(merchant.value) }
    }

    fun findCategoryByName(name: String): Category? {
        return repository.getAll()
            .firstOrNull { it.name.equals(name, true) }
    }

    private fun List<Merchant>.containsIgnoreCase(value: String): Boolean =
        this.any { it.value.equals(value, true) }
}