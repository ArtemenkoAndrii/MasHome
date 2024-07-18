package com.mas.mobile.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], name = "index_categories_on_name")]
)
data class Category(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "plan")
    val plan: Double,

    @ColumnInfo(name = "active")
    val isActive: Boolean,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "merchants")
    val merchants: List<String>
)