package com.mas.mobile.repository.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mas.mobile.domain.budget.Category
import com.mas.mobile.domain.budget.CategoryId
import com.mas.mobile.domain.budget.CategoryLiveData
import com.mas.mobile.domain.budget.CategoryRepository
import com.mas.mobile.domain.budget.Merchant
import com.mas.mobile.repository.db.config.AppDatabase
import javax.inject.Singleton
import com.mas.mobile.repository.db.entity.Category as CategoryData

@Singleton
class CategoryRepositoryImpl(
    val db: AppDatabase
) : CategoryRepository {
    private val dao = db.categoryDAO()
    override val live: CategoryLiveData
        get() = CategoryLiveDataImpl(db)

    override fun getAll(): List<Category> = dao.getAll().map { it.toModel() }

    override fun getById(id: CategoryId): Category? = dao.getById(id.value)?.toModel()

    override fun create(): Category =
        Category(
            id = CategoryId(db.idGeneratorDAO().generateId().toInt()),
            name = "",
            plan = 0.0,
            isActive = false,
            description = "",
            merchants = mutableListOf()
        )

    override suspend fun save(item: Category) {
        dao.upsert(item.toDTO())
    }

    override suspend fun remove(item: Category) {
        dao.delete(item.toDTO())
    }
}

class CategoryLiveDataImpl(val db: AppDatabase) : CategoryLiveData {
    val dao = db.categoryDAO()

    override fun getAll(): LiveData<List<Category>> =
        Transformations.map(dao.getAllLive()) { category ->
            category.map { it.toModel() }.toList()
        }
}

private fun Category.toDTO() = CategoryMapper.toDTO(this)
private fun CategoryData.toModel() = CategoryMapper.toModel(this)