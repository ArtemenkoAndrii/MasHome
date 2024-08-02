package com.mas.mobile.domain.budget

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CategoryServiceTest {
    private val mockCategoryRepository = mockk<CategoryRepository>(relaxed = true)

    private val testInstance = CategoryService(mockCategoryRepository)

    @BeforeEach
    fun setUp() {
        every { mockCategoryRepository.getAll() } returns listOf(
            Category(
                id = CategoryId(1),
                name = "Food",
                plan = 100.00,
                isActive = true,
                description = "All about food",
                merchants = mutableListOf(Merchant("McDonalds"), Merchant("Starbucks"))
            ),
            Category(
                id = CategoryId(2),
                name = "Petrol",
                plan = 200.00,
                isActive = true,
                description = "All about petrol",
                merchants = mutableListOf(Merchant("Shell"))
            )
        )
    }

    @Test
    fun `should find category by name ignoring case` () {
        assertEquals("Food", testInstance.findCategoryByName("food")!!.name)
        assertNull(testInstance.findCategoryByName("non-existing"))
    }

    @Test
    fun `should find category by merchant ignoring case` () {
        assertEquals("Petrol", testInstance.findCategoryByMerchant(Merchant("shell"))!!.name)
        assertNull(testInstance.findCategoryByMerchant(Merchant("non-existing")))
    }
}