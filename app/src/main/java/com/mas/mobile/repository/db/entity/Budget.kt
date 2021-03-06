package com.mas.mobile.repository.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "budgets", indices = [Index(value = ["name"], unique = true)])
data class Budget(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    override var id: Int = 0,

    @ColumnInfo(name = "name")
    override var name: String = "",

    @ColumnInfo(name = "plan")
    var plan: Double = 0.0,

    @ColumnInfo(name = "fact")
    var fact: Double = 0.0,

    @ColumnInfo(name = "startsOn")
    var startsOn: LocalDate = LocalDate.now(),

    @ColumnInfo(name = "isActive")
    var isActive: Boolean = false,

    @ColumnInfo(name = "comment")
    var comment: String? = ""
): Searchable(id, name)
