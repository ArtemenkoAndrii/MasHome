package com.mas.mobile.repository.budget

import com.mas.mobile.domain.budget.Category
import com.mas.mobile.domain.budget.CategoryId
import com.mas.mobile.domain.budget.Merchant
import com.mas.mobile.repository.db.entity.Category as CategoryData

object CategoryMapper {
    fun toModel(dto: CategoryData): Category =
        Category(
            id = CategoryId(dto.id),
            name = dto.name,
            plan = dto.plan,
            isActive = dto.isActive,
            description = dto.description,
            merchants = dto.merchants.map { Merchant(it) }
        )

    fun toDTO(model: Category): CategoryData =
        CategoryData(
            id = model.id.value,
            name = model.name,
            plan = model.plan,
            isActive = model.isActive,
            description = model.description,
            merchants = model.merchants.map { it.value }
        )
}
