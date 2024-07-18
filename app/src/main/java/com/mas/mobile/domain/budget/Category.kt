package com.mas.mobile.domain.budget

data class Category(
    val id: CategoryId,
    var name: String,
    var plan: Double,
    var isActive: Boolean,
    var description: String,
    var merchants: List<Merchant>
)

@JvmInline
value class CategoryId(val value: Int)

@JvmInline
value class Merchant(val value: String)